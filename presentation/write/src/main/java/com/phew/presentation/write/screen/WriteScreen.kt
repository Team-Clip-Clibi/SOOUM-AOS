package com.phew.presentation.write.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phew.core_design.AppBar
import androidx.compose.ui.platform.LocalContext
import com.phew.core.ui.R
import com.phew.core_design.DialogComponent
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent
import com.phew.core_design.component.button.RoundButton
import com.phew.core_design.component.card.BaseCardData
import com.phew.core_design.component.card.CardView
import com.phew.core.ui.model.CameraCaptureRequest
import com.phew.core.ui.model.CameraPickerAction
import com.phew.core.ui.component.camera.CameraPickerBottomSheet
import com.phew.core.ui.component.camera.CameraPickerEffect
import com.phew.core.ui.model.CameraPickerEffectState
import com.phew.presentation.write.model.BackgroundConfig
import com.phew.presentation.write.model.FontConfig
import com.phew.presentation.write.model.FontItem
import com.phew.presentation.write.model.WriteOption
import com.phew.presentation.write.model.WriteOptions
import com.phew.presentation.write.screen.component.FilteredImageGrid
import com.phew.presentation.write.screen.component.FontSelectorGrid
import com.phew.presentation.write.viewmodel.WriteViewModel
import androidx.compose.ui.res.stringResource

/**
 *  추후 작업
 *  1. 완료 되면 어디로 이동해야 하는지
 */
@Composable
internal fun WriteRoute(
    modifier: Modifier = Modifier,
    viewModel: WriteViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    BackHandler {
        onBackPressed()
    }

    val context = LocalContext.current

    //   위치 권한
    val locationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionResult ->
            val isGranted = permissionResult.any { it.value }
            viewModel.onLocationPermissionResult(isGranted)
        }
    )

    LaunchedEffect(context) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.onInitialLocationPermissionCheck(fineGranted || coarseGranted)
    }
    
    //  Effect Event로 수정
    LaunchedEffect(Unit) {
        viewModel.requestPermissionEvent.collect { permissions ->
            locationPermission.launch(permissions)
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    WriteScreen(
        modifier = modifier,
        content = uiState.content,
        tags = uiState.tags,
        isWriteCompleted = uiState.canComplete,
        activeBackgroundImageResId = uiState.activeBackgroundResId,
        activeBackgroundUri = uiState.activeBackgroundUri,
        selectedBackgroundFilter = uiState.selectedBackgroundFilter,
        selectedGridImageResId = uiState.currentFilterSelection,
        selectedFont = uiState.selectedFont,
        selectedFontFamily = uiState.selectedFontFamily,
        selectedOptionId = uiState.selectedOptionId,
        hasLocationPermission = uiState.hasLocationPermission,
        showLocationPermissionDialog = uiState.showLocationPermissionDialog,
        onBackPressed = onBackPressed,
        onContentChange = viewModel::updateContent,
        onContentEnter = viewModel::onContentEnter,
        onFilterChange = viewModel::selectBackgroundFilter,
        onImageSelected = viewModel::selectBackgroundImage,
        onCustomImageSelected = viewModel::onBackgroundAlbumImagePicked,
        onFontSelected = viewModel::selectFont,
        onOptionSelected = viewModel::selectOption,
        onDistanceOptionWithoutPermission = viewModel::onDistanceOptionClickWithoutPermission,
        onDismissLocationDialog = viewModel::dismissLocationPermissionDialog,
        onRequestLocationPermission = viewModel::requestLocationPermission,
        onAddTag = viewModel::addTag,
        onRemoveTag = viewModel::removeTag,
        focusTagInput = uiState.focusTagInput,
        onTagFocusHandled = viewModel::onTagInputFocusHandled,
        onWriteComplete = viewModel::onWriteComplete,
        showBackgroundPicker = uiState.showBackgroundPickerSheet,
        shouldLaunchAlbum = uiState.shouldLaunchBackgroundAlbum,
        shouldRequestCameraPermission = uiState.shouldRequestBackgroundCameraPermission,
        pendingCameraCapture = uiState.pendingBackgroundCameraCapture,
        onCameraPickerRequested = viewModel::onBackgroundPickerRequested,
        onCameraPickerDismissed = viewModel::onBackgroundPickerDismissed,
        onCameraPickerAction = viewModel::onBackgroundPickerAction,
        onAlbumRequestConsumed = viewModel::onBackgroundAlbumRequestConsumed,
        onCameraPermissionRequestConsumed = viewModel::onBackgroundCameraPermissionRequestConsumed,
        onCameraPermissionResult = viewModel::onBackgroundCameraPermissionResult,
        onCameraCaptureLaunched = viewModel::onBackgroundCameraCaptureLaunched,
        onCameraCaptureResult = viewModel::onBackgroundCameraCaptureResult
    )
}

@Composable
private fun WriteScreen(
    modifier: Modifier = Modifier,
    content: String,
    tags: List<String>,
    isWriteCompleted: Boolean,
    activeBackgroundImageResId: Int?,
    activeBackgroundUri: Uri?,
    selectedBackgroundFilter: String,
    selectedGridImageResId: Int?,
    selectedFont: String,
    selectedFontFamily: FontFamily?,
    selectedOptionId: String,
    hasLocationPermission: Boolean,
    showLocationPermissionDialog: Boolean,
    onBackPressed: () -> Unit,
    onContentChange: (String) -> Unit,
    onContentEnter: () -> Unit,
    onFilterChange: (filter: String) -> Unit,
    onImageSelected: (Int) -> Unit,
    onCustomImageSelected: (Uri) -> Unit,
    onFontSelected: (FontFamily) -> Unit,
    onOptionSelected: (String) -> Unit,
    onDistanceOptionWithoutPermission: () -> Unit,
    onDismissLocationDialog: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    focusTagInput: Boolean,
    onTagFocusHandled: () -> Unit,
    onWriteComplete: () -> Unit,
    showBackgroundPicker: Boolean,
    shouldLaunchAlbum: Boolean,
    shouldRequestCameraPermission: Boolean,
    pendingCameraCapture: CameraCaptureRequest?,
    onCameraPickerRequested: () -> Unit,
    onCameraPickerDismissed: () -> Unit,
    onCameraPickerAction: (CameraPickerAction) -> Unit,
    onAlbumRequestConsumed: () -> Unit,
    onCameraPermissionRequestConsumed: () -> Unit,
    onCameraPermissionResult: (Boolean) -> Unit,
    onCameraCaptureLaunched: (CameraCaptureRequest) -> Unit,
    onCameraCaptureResult: (Boolean, Uri) -> Unit
) {

    val snackBarHostState = remember { SnackbarHostState() }
    val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
    val albumPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val permissionMessage = stringResource(com.phew.core_design.R.string.common_permission)

    CameraPickerEffect(
        effectState = CameraPickerEffectState(
            launchAlbum = shouldLaunchAlbum,
            requestCameraPermission = shouldRequestCameraPermission,
            pendingCapture = pendingCameraCapture
        ),
        snackBarHostState = snackBarHostState,
        onAlbumRequestConsumed = onAlbumRequestConsumed,
        onAlbumPicked = onCustomImageSelected,
        onCameraPermissionRequestConsumed = onCameraPermissionRequestConsumed,
        onCameraPermissionResult = onCameraPermissionResult,
        onCameraCaptureLaunched = onCameraCaptureLaunched,
        onCameraCaptureResult = onCameraCaptureResult,
        cameraPermissions = cameraPermissions,
        albumPermissions = albumPermissions,
        albumDeniedMessage = permissionMessage,
        cameraDeniedMessage = permissionMessage
    )

    Scaffold (
        modifier = modifier,
        topBar = {
            AppBar.TextButtonAppBar(
                appBarText = "새로운 카드",
                buttonText = "완료",
                onButtonClick = onWriteComplete,
                onClick = onBackPressed,
                buttonTextColor = if (isWriteCompleted) NeutralColor.BLACK else NeutralColor.GRAY_300
            )
        },
        bottomBar = {
            OptionButtons(
                options = WriteOptions.availableOptions,
                selectedOptionId = selectedOptionId,
                hasLocationPermission = hasLocationPermission,
                onOptionSelected = { option -> onOptionSelected(option.id) },
                onDistancePermissionRequest = onDistanceOptionWithoutPermission
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState) { data ->
                DialogComponent.SnackBar(data)
            }
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .background(NeutralColor.WHITE)
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //  TODO content에서 Tag 포커스 이동 추가 수정
            CardView(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 328.dp, max = 420.dp),
                data = BaseCardData.Write(
                    content = content,
                    tags = tags,
                    backgroundResId = activeBackgroundImageResId,
                    backgroundUri = activeBackgroundUri,
                    fontFamily = selectedFontFamily,
                    onContentChange = onContentChange,
                    onContentEnter = onContentEnter,
                    onAddTag = onAddTag,
                    onRemoveTag = onRemoveTag,
                    shouldFocusTagInput = focusTagInput,
                    onTagFocusHandled = onTagFocusHandled
                )
            )
            // TODO 빠르게 누를시 이전 내용 남아 있음
            BackgroundSelect(
                modifier = Modifier.fillMaxWidth(),
                selectedGridImageResId = selectedGridImageResId,
                selectedBackgroundFilter = selectedBackgroundFilter,
                onFilterChange = onFilterChange,
                onImageSelected = onImageSelected,
                onCameraClick = onCameraPickerRequested
            )

            FontSelect(
                fontItem = FontConfig.availableFonts,
                selectedFont = selectedFont,
                onFontSelected = onFontSelected
            )
        }
    }

    if (showLocationPermissionDialog) {
        DialogComponent.DefaultButtonTwo(
            title = stringResource(R.string.location_permission_title),
            description = stringResource(R.string.location_permission_description),
            buttonTextStart = stringResource(R.string.location_permission_negative),
            buttonTextEnd = stringResource(R.string.location_permission_positive),
            onClick = {
                onRequestLocationPermission()
                onDismissLocationDialog()
            },
            onDismiss = onDismissLocationDialog
        )
    }

    CameraPickerBottomSheet(
        visible = showBackgroundPicker,
        onActionSelected = onCameraPickerAction,
        onDismiss = onCameraPickerDismissed
    )
}

@Composable
private fun BackgroundSelect(
    modifier: Modifier,
    selectedGridImageResId: Int?,
    selectedBackgroundFilter: String,
    onFilterChange: (filter: String) -> Unit,
    onImageSelected: (Int) -> Unit,
    onCameraClick: () -> Unit
) {
    Column {
        Text(
            text = "배경",
            style = TextComponent.CAPTION_1_SB_12.copy(color = Primary.DARK)
        )

        FilteredImageGrid(
            filters = BackgroundConfig.filterNames,
            imagesByFilter = BackgroundConfig.imagesByFilter,
            selectedFilter = selectedBackgroundFilter,
            selectedImage = selectedGridImageResId,
            onFilterSelected = { filter ->
                onFilterChange(filter)
            },
            onImageSelected = onImageSelected,
            onCameraClick = onCameraClick
        )
    }
}

@Composable
private fun FontSelect(
    fontItem: List<FontItem>,
    selectedFont: String,
    onFontSelected: (FontFamily) -> Unit
) {
    Column {
        Text(
            text = "폰트",
            style = TextComponent.CAPTION_1_SB_12.copy(color = Primary.DARK)
        )
        
        FontSelectorGrid(
            fonts = fontItem,
            selectedFont = selectedFont,
            onFontSelected = onFontSelected
        )
    }
}

@Composable
private fun OptionButtons(
    options: List<WriteOption>,
    selectedOptionId: String,
    hasLocationPermission: Boolean,
    onOptionSelected: (WriteOption) -> Unit,
    onDistancePermissionRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column (
        modifier = modifier
            .fillMaxWidth()
            .background(NeutralColor.WHITE)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NeutralColor.GRAY_200)
                .height(1.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEach { option ->
                val isDistanceOption = option.id == WriteOptions.DISTANCE_OPTION_ID
                val enabled = !isDistanceOption || hasLocationPermission
                Box {
                    RoundButton(
                        text = option.displayName,
                        selected = option.id == selectedOptionId,
                        enabled = enabled,
                        onClick = {
                            onOptionSelected(option)
                        }
                    )
                    if (!enabled) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onDistancePermissionRequest() }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoundButtonPreview() {
    var selected by remember { mutableStateOf(WriteOptions.availableOptions.first().id) }

    OptionButtons(
        options = WriteOptions.availableOptions,
        selectedOptionId = selected,
        hasLocationPermission = true,
        onOptionSelected = { option -> selected = option.id },
        onDistancePermissionRequest = {}
    )
}


private const val TAG = "WriteScreen"
