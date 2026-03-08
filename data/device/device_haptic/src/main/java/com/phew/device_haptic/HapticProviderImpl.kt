package com.phew.device_haptic

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class HapticProviderImpl @Inject constructor(@param:ApplicationContext private val context: Context) :
    HapticProvider {
    override fun haptic() {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator
        val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
        vibrator.vibrate(effect)
    }
}