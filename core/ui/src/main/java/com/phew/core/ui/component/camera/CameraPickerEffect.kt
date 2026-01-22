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
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.phew.core.ui.model.CameraCaptureRequest
import com.phew.core.ui.model.CameraPickerEffectState
import com.phew.core_common.log.SooumLog
import com.phew.core.ui.clarity.LocalSessionRecorder

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
    mediaRequest: PickVisualMediaRequest = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
) {
    val context = LocalContext.current
    val anyAlbumPermission = albumPermissions.isNotEmpty()
    val anyCameraPermission = cameraPermissions.isNotEmpty()
    val sessionRecorder = LocalSessionRecorder.current
    var activeCapture: CameraCaptureRequest? by remember { mutableStateOf(null) }
    val albumCropImage = rememberLauncherForActivityResult(
        contract = CropImageContract(),
        onResult = { result ->
            val cropped = result.uriContent ?: return@rememberLauncherForActivityResult
            onAlbumPicked(cropped)
        }
    )
    val cameraCropImage = rememberLauncherForActivityResult(
        contract = CropImageContract(),
        onResult = { result ->
            val croppedUri = result.uriContent
            val originalUri = activeCapture?.uri

            if (croppedUri != null) {
                onCameraCaptureResult(true, croppedUri)
                if (originalUri != null) {
                    try {
                        context.contentResolver.delete(originalUri, null, null)
                    } catch (e: Exception) {
                        SooumLog.e(TAG, "Failed to delete temp original image: $e")
                    }
                }
            } else {
                if (originalUri != null) {
                    SooumLog.d(TAG, "Crop cancelled. Using original image.")
                    onCameraCaptureResult(true, originalUri)
                } else {
                    onCameraCaptureResult(false, Uri.EMPTY)
                }
            }
            activeCapture = null
        }
    )

    val albumLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            sessionRecorder.resume()
            if (uri != null) {
                albumCropImage.launch(
                    CropImageContractOptions(
                        uri = uri,
                        cropImageOptions = cropOption()
                    )
                )
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
                sessionRecorder.pause()
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
            sessionRecorder.resume()
            val capture = activeCapture
            if (capture != null) {
                if (!success) {
                    context.contentResolver.delete(capture.uri, null, null)
                    onCameraCaptureResult(false,  Uri.EMPTY)
                    return@rememberLauncherForActivityResult
                }
                cameraCropImage.launch(
                    CropImageContractOptions(
                        uri = capture.uri,
                        cropImageOptions = cropOption()
                    )
                )
            }
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

fun cropOption(): CropImageOptions {
    return CropImageOptions().apply {
        fixAspectRatio = true
        aspectRatioX = 1
        aspectRatioY = 1
        scaleType = CropImageView.ScaleType.FIT_CENTER
        allowFlipping = false
        allowRotation = false
        maxZoom = 1
        autoZoomEnabled = false
        initialCropWindowPaddingRatio = 0.1f
        activityTitle = ""
        activityMenuIconColor = android.graphics.Color.BLACK
        activityBackgroundColor = android.graphics.Color.WHITE
    }
}

private const val TAG = "CameraPickerEffect"