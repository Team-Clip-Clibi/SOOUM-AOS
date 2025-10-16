package com.phew.sign_up

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.phew.core_design.AppBar
import com.phew.core_design.LargeButton
import com.phew.core_design.NeutralColor
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.phew.core_common.ERROR
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_design.AvatarComponent
import com.phew.core_design.DialogComponent
import com.phew.core_design.TextComponent
import com.phew.core.ui.component.camera.CameraPickerBottomSheet
import com.phew.core.ui.component.camera.CameraPickerEffect
import com.phew.core.ui.model.CameraPickerEffectState


@Composable
fun ProfileImageView(viewModel: SignUpViewModel, onBack: () -> Unit, nexPage: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    BackHandler {
        onBack()
    }
    LaunchedEffect(uiState) {
        when(val result = uiState.signUp){
            is UiState.Fail ->{
                when(result.errorMessage){
                    ERROR_NETWORK -> {
                        snackBarHostState.showSnackbar(
                            message = context.getString(com.phew.core_design.R.string.error_network),
                            duration = SnackbarDuration.Short
                        )
                    }
                    ERROR -> {
                        snackBarHostState.showSnackbar(
                            message = context.getString(com.phew.core_design.R.string.error_app),
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
            is UiState.Success-> {
                nexPage()
            }
            else -> Unit
        }
    }

    val permissionMessage = context.getString(com.phew.core_design.R.string.common_permission)
    val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
    val albumPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    CameraPickerEffect(
        effectState = CameraPickerEffectState(
            launchAlbum = uiState.shouldLaunchProfileAlbum,
            requestCameraPermission = uiState.shouldRequestProfileCameraPermission,
            pendingCapture = uiState.pendingProfileCameraCapture
        ),
        snackBarHostState = snackBarHostState,
        onAlbumRequestConsumed = viewModel::onProfileAlbumRequestConsumed,
        onAlbumPicked = viewModel::onAlbumImagePicked,
        onCameraPermissionRequestConsumed = viewModel::onProfileCameraPermissionRequestConsumed,
        onCameraPermissionResult = viewModel::onProfileCameraPermissionResult,
        onCameraCaptureLaunched = viewModel::onProfileCameraCaptureLaunched,
        onCameraCaptureResult = { success, uri ->
            viewModel.onProfileCameraCaptureResult(success, uri)
        },
        cameraPermissions = cameraPermissions,
        albumPermissions = albumPermissions,
        albumDeniedMessage = permissionMessage,
        cameraDeniedMessage = permissionMessage
    )

    Scaffold(
        topBar = {
            AppBar.IconLeftAppBar(
                onClick = onBack, appBarText = stringResource(R.string.signUp_app_bar)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(color = NeutralColor.WHITE)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                verticalAlignment = Alignment.Top,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    LargeButton.NoIconSecondary(
                        onClick = viewModel::signUp,
                        buttonText = stringResource(com.phew.core_design.R.string.common_skip)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    LargeButton.NoIconPrimary(
                        onClick = viewModel::signUp,
                        buttonText = stringResource(com.phew.core_design.R.string.common_finish)
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState) { data ->
                DialogComponent.SnackBar(data)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = NeutralColor.WHITE)
                .padding(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = paddingValues.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {
            TitleView()
            ContentView(
                onClick = viewModel::updateProfileBottom,
                url = uiState.profile
            )
        }
    }

    CameraPickerBottomSheet(
        visible = uiState.profileBottom,
        onActionSelected = viewModel::onProfilePickerAction,
        onDismiss = viewModel::updateProfileBottom,
        albumTextRes = R.string.signUp_picture_bottom_item_album,
        cameraTextRes = R.string.signUp_picture_bottom_item_camera
    )
}

@Composable
private fun TitleView() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Component.PageNumber("1", isSelect = true)
        Component.PageNumber("2", isSelect = true)
        Component.PageNumber("3", isSelect = true)
    }
    Text(
        text = stringResource(R.string.signUp_picture_title),
        style = TextComponent.HEAD_2_B_24,
        color = NeutralColor.BLACK
    )
}

@Composable
private fun ContentView(onClick: () -> Unit, url: Uri) {
    Spacer(modifier = Modifier.height(32.dp))
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AvatarComponent.LargeAvatar(
            onClick = {
                onClick()
            },
            url = url
        )
    }
}
