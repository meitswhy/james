package com.civicai.app.worker

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.civicai.app.data.local.AppDatabase
import com.civicai.app.data.local.DocumentEntity
import com.civicai.app.data.parser.DocumentParser
import com.civicai.app.data.parser.UriUtils

class DocumentIndexingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val docId = inputData.getString(KEY_DOC_ID) ?: return Result.failure()
        val docUriString = inputData.getString(KEY_DOC_URI) ?: return Result.failure()
        val rawTitle = inputData.getString(KEY_DOC_TITLE) ?: "Document"

        val db = AppDatabase.getDatabase(applicationContext)
        val docDao = db.documentDao()
        val chunkDao = db.chunkDao()

        return try {
            setProgress(workDataOf(KEY_PROGRESS to 10))

            val uri = Uri.parse(docUriString)
            val realTitle = UriUtils.getRealDisplayName(applicationContext, uri).ifBlank { rawTitle }

            // Copy file to app-private storage for persistent indexing
            val copiedFile = UriUtils.copyUriToPrivateStorage(applicationContext, uri, docId, realTitle)

            docDao.insertDocument(
                DocumentEntity(
                    id = docId,
                    title = realTitle,
                    mimeType = applicationContext.contentResolver.getType(uri) ?: "application/octet-stream",
                    uri = copiedFile?.absolutePath ?: docUriString,
                    pageCount = 0,
                    chunkCount = 0,
                    addedTimestamp = System.currentTimeMillis(),
                    indexStatus = "INDEXING"
                )
            )

            setProgress(workDataOf(KEY_PROGRESS to 40))

            val parser = DocumentParser(applicationContext)
            val parseInput: Any = copiedFile ?: uri
            val result = parser.parseDocument(parseInput, docId, realTitle)

            setProgress(workDataOf(KEY_PROGRESS to 70))

            if (!result.isSuccess || result.chunks.isEmpty()) {
                docDao.insertDocument(
                    DocumentEntity(
                        id = docId,
                        title = result.title,
                        mimeType = result.mimeType,
                        uri = copiedFile?.absolutePath ?: docUriString,
                        pageCount = 0,
                        chunkCount = 0,
                        addedTimestamp = System.currentTimeMillis(),
                        indexStatus = "ERROR",
                        errorMessage = result.errorMessage ?: "No readable text found in file"
                    )
                )
                return Result.failure(workDataOf(KEY_ERROR to (result.errorMessage ?: "Parsing failed")))
            }

            // Insert valid chunks into Room FTS and synchronize virtual table
            chunkDao.insertChunks(result.chunks)
            try {
                chunkDao.rebuildFts()
            } catch (e: Exception) {
                com.civicai.app.data.local.LocalDiagnosticsLogger.w("DocumentIndexingWorker", "FTS rebuild warning: ${e.message}")
            }

            docDao.insertDocument(
                DocumentEntity(
                    id = docId,
                    title = result.title,
                    mimeType = result.mimeType,
                    uri = copiedFile?.absolutePath ?: docUriString,
                    pageCount = result.pageCount,
                    chunkCount = result.chunks.size,
                    addedTimestamp = System.currentTimeMillis(),
                    indexStatus = "INDEXED",
                    errorMessage = null
                )
            )

            setProgress(workDataOf(KEY_PROGRESS to 100))
            Result.success(workDataOf(KEY_CHUNKS_COUNT to result.chunks.size))

        } catch (e: Exception) {
            e.printStackTrace()
            docDao.insertDocument(
                DocumentEntity(
                    id = docId,
                    title = rawTitle,
                    mimeType = "unknown",
                    uri = docUriString,
                    pageCount = 0,
                    chunkCount = 0,
                    addedTimestamp = System.currentTimeMillis(),
                    indexStatus = "ERROR",
                    errorMessage = e.localizedMessage ?: "Failed to index document"
                )
            )
            Result.failure(workDataOf(KEY_ERROR to (e.localizedMessage ?: "Indexing error")))
        }
    }

    companion object {
        const val KEY_DOC_ID = "KEY_DOC_ID"
        const val KEY_DOC_URI = "KEY_DOC_URI"
        const val KEY_DOC_TITLE = "KEY_DOC_TITLE"
        const val KEY_PROGRESS = "KEY_PROGRESS"
        const val KEY_CHUNKS_COUNT = "KEY_CHUNKS_COUNT"
        const val KEY_ERROR = "KEY_ERROR"

        fun enqueueIndexing(context: Context, docId: String, uri: Uri, title: String) {
            val realTitle = UriUtils.getRealDisplayName(context, uri).ifBlank { title }
            val inputData = workDataOf(
                KEY_DOC_ID to docId,
                KEY_DOC_URI to uri.toString(),
                KEY_DOC_TITLE to realTitle
            )
            val workRequest = OneTimeWorkRequestBuilder<DocumentIndexingWorker>()
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "index_$docId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
