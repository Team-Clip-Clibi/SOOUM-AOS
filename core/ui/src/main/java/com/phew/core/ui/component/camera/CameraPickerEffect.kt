package com.phew.core.ui.component.camera

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.phew.core.ui.model.CameraCaptureRequest
import com.phew.core.ui.model.CameraPickerEffectState
import com.phew.core_common.log.SooumLog

private val DefaultAlbumPermissions: Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

@Composable
fun CameraPickerEffect(
    effectState: CameraPickerEffectState,
    onAlbumRequestConsumed: () -> Unit,
    onAlbumPicked: (Uri) -> Unit,
    onCameraPermissionRequestConsumed: () -> Unit,
    onCameraPermissionResult: (Boolean) -> Unit,
    onCameraCaptureLaunched: (CameraCaptureRequest) -> Unit,
    onCameraCaptureResult: (Boolean, Uri) -> Unit,
    cameraPermissions: Array<String> = arrayOf(Manifest.permission.CAMERA),
    albumPermissions: Array<String> = DefaultAlbumPermissions,
    onCameraPermissionDenied: () -> Unit = {},
    onGalleryPermissionDenied: () -> Unit = {},
    mediaRequest: PickVisualMediaRequest = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
) {
    val context = LocalContext.current
    val anyAlbumPermission = albumPermissions.isNotEmpty()
    val anyCameraPermission = cameraPermissions.isNotEmpty()

    var activeCapture: CameraCaptureRequest? by remember { mutableStateOf(null) }

    val albumLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onAlbumPicked(uri)
            }
        }
    )

    val albumPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { result ->
            val granted = albumPermissions.all { permission ->
                result[permission] ?: false
            }
            SooumLog.d(TAG, "album permission result : $granted")
            if (granted) {
                albumLauncher.launch(mediaRequest)
            } else {
                onGalleryPermissionDenied()
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { result ->
            val granted = cameraPermissions.all { permission ->
                result[permission] ?: false
            }
            onCameraPermissionResult(granted)
            if (!granted) {
                onCameraPermissionDenied()
            }
        }
    )

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            val capture = activeCapture
            if (capture != null) {
                if (!success) {
                    context.contentResolver.delete(capture.uri, null, null)
                }
                onCameraCaptureResult(success, capture.uri)
            }
            activeCapture = null
        }
    )

    LaunchedEffect(effectState.launchAlbum) {
        if (effectState.launchAlbum) {
            if (anyAlbumPermission) {
                albumPermissionLauncher.launch(albumPermissions)
            } else {
                albumLauncher.launch(mediaRequest)
            }
            onAlbumRequestConsumed()
        }
    }

    LaunchedEffect(effectState.requestCameraPermission) {
        if (effectState.requestCameraPermission) {
            if (anyCameraPermission) {
                cameraPermissionLauncher.launch(cameraPermissions)
            } else {
                onCameraPermissionResult(true)
            }
            onCameraPermissionRequestConsumed()
        }
    }

    LaunchedEffect(effectState.pendingCapture?.id) {
        val request = effectState.pendingCapture ?: return@LaunchedEffect
        activeCapture = request
        onCameraCaptureLaunched(request)
        takePictureLauncher.launch(request.uri)
    }
}

private const val TAG = "CameraPickerEffect"