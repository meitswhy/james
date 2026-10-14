# ProGuard Rules for CivicAI On-Device ML Kit, Room, DataStore, and Coroutines

# Keep ML Kit Text Recognition & Vision
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text.** { *; }
-dontwarn com.google.mlkit.**

# Keep Room DB & SQLite FTS
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface *
-keepclassmembers class * {
    @androidx.room.Query <methods>;
    @androidx.room.Insert <methods>;
    @androidx.room.Update <methods>;
    @androidx.room.Delete <methods>;
}
-dontwarn androidx.room.**

# Keep DataStore & Proto Serializers
-keep class androidx.datastore.** { *; }

# Keep Coroutines & Play Services Tasks
-keep class kotlinx.coroutines.** { *; }
-keep class com.google.android.gms.tasks.** { *; }

# Keep CivicAI Entities & Models
-keep class com.civicai.app.data.** { *; }
