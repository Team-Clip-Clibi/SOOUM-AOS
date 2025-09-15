package com.phew.sign_up

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.phew.core_design.AvatarComponent
import com.phew.core_design.BottomSheetComponent
import com.phew.core_design.BottomSheetItem
import com.phew.core_design.DialogComponent
import com.phew.core_design.TextComponent
import kotlinx.coroutines.launch


@Composable
fun ProfileImageView(viewModel: SignUpViewModel, onBack: () -> Unit, nexPage: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    ImagePickerEffect(
        viewModel = viewModel,
        uiState = uiState,
        snackBarHostState = snackBarHostState
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
                        onClick = nexPage,
                        buttonText = stringResource(com.phew.core_design.R.string.common_skip)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    LargeButton.NoIconPrimary(
                        onClick = nexPage,
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
}

@Composable
private fun ImagePickerEffect(
    viewModel: SignUpViewModel,
    uiState: SignUp,
    snackBarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val pickSingleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateProfile(it) }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        cameraImageUri?.let { uri ->
            if (success) {
                viewModel.updateProfile(uri)
            } else {
                context.contentResolver.delete(uri, null, null)
            }
        }
        cameraImageUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.createImage()
        } else {
            scope.launch {
                snackBarHostState.showSnackbar(
                    message = context.getString(com.phew.core_design.R.string.common_permission)
                )
            }
        }
    }

    LaunchedEffect(uiState.createImageFile) {
        val result = uiState.createImageFile
        if (result is UiState.Success) {
            cameraImageUri = result.data
            takePictureLauncher.launch(result.data)
        }
    }

    if (uiState.profileBottom) {
        ShowBottomSheet(
            album = {
                pickSingleLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            camera = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
            onDismiss = viewModel::updateProfileBottom
        )
    }
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

@Composable
private fun ShowBottomSheet(
    album: () -> Unit,
    camera: () -> Unit,
    onDismiss: () -> Unit,
) {
    BottomSheetComponent.BottomSheet(
        data = arrayListOf(
            BottomSheetItem(
                id = PROFILE_ALBUM,
                title = stringResource(R.string.signUp_picture_bottom_item_album)
            ),
            BottomSheetItem(
                id = PROFILE_PICTURE,
                title = stringResource(R.string.signUp_picture_bottom_item_camera)
            )
        ),
        onItemClick = { id ->
            when (id) {
                PROFILE_ALBUM -> album()
                PROFILE_PICTURE -> camera()
            }
        },
        onDismiss = onDismiss
    )
}
