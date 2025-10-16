package com.phew.core.ui.component.camera

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.phew.core.ui.model.CameraCaptureRequest
import com.phew.core.ui.model.CameraPickerEffectState
import kotlinx.coroutines.launch

@Composable
fun CameraPickerEffect(
    effectState: CameraPickerEffectState,
    snackBarHostState: SnackbarHostState,
    onAlbumRequestConsumed: () -> Unit,
    onAlbumPicked: (Uri) -> Unit,
    onCameraPermissionRequestConsumed: () -> Unit,
    onCameraPermissionResult: (Boolean) -> Unit,
    onCameraCaptureLaunched: (CameraCaptureRequest) -> Unit,
    onCameraCaptureResult: (Boolean, Uri) -> Unit,
    cameraPermissions: Array<String> = arrayOf(Manifest.permission.CAMERA),
    albumPermissions: Array<String> = emptyArray(),
    albumDeniedMessage: String? = null,
    cameraDeniedMessage: String? = null,
    mediaRequest: PickVisualMediaRequest = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
            if (granted) {
                albumLauncher.launch(mediaRequest)
            } else if (!albumDeniedMessage.isNullOrEmpty()) {
                scope.launch {
                    snackBarHostState.showSnackbar(
                        message = albumDeniedMessage,
                        duration = SnackbarDuration.Short
                    )
                }
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
            if (!granted && !cameraDeniedMessage.isNullOrEmpty()) {
                scope.launch {
                    snackBarHostState.showSnackbar(
                        message = cameraDeniedMessage,
                        duration = SnackbarDuration.Short
                    )
                }
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
