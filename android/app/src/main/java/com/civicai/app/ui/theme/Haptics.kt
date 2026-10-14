package com.civicai.app.ui.theme

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticFeedbackHelper(private val context: Context, private val enabledProvider: () -> Boolean) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun lightClick() {
        if (!enabledProvider()) return
        vibrate(10, 40)
    }

    fun mediumClick() {
        if (!enabledProvider()) return
        vibrate(20, 80)
    }

    fun heavyClick() {
        if (!enabledProvider()) return
        vibrate(35, 150)
    }

    fun successPattern() {
        if (!enabledProvider()) return
        vibrate(25, 100)
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(durationMs)
                    }
                }
            }
        } catch (e: Throwable) {
            // Silently swallow hardware/permission vibration issues to ensure UI clicks never crash
        }
    }
}
