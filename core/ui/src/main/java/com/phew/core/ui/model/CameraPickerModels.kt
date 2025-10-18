package com.phew.core.ui.model

import android.net.Uri

/**
 * 카메라 혹은 앨범 선택과 관련된 공통 모델 정의.
 */
enum class CameraPickerAction {
    Album,
    Camera
}

data class CameraCaptureRequest(
    val id: Long,
    val uri: Uri
)

data class CameraPickerEffectState(
    val launchAlbum: Boolean = false,
    val requestCameraPermission: Boolean = false,
    val pendingCapture: CameraCaptureRequest? = null
)
