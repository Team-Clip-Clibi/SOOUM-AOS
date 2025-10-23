package com.phew.presentation.write.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.phew.core.ui.R
import com.phew.core.ui.component.camera.CameraPickerBottomSheet
import com.phew.core.ui.component.camera.CameraPickerEffect
import com.phew.core.ui.model.CameraCaptureRequest
import com.phew.core.ui.model.CameraPickerAction
import com.phew.core.ui.model.CameraPickerEffectState
import com.phew.core_design.AppBar
import com.phew.core_design.DialogComponent
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent
import com.phew.core_design.component.button.RoundButton
import com.phew.core_design.component.card.BaseCardData
import com.phew.core_design.component.card.CardView
import com.phew.presentation.write.component.NumberTagFlowLayout
import com.phew.presentation.write.component.NumberTagItem
import com.phew.presentation.write.model.BackgroundConfig
import com.phew.presentation.write.model.FontConfig
import com.phew.presentation.write.model.FontItem
import com.phew.presentation.write.model.WriteOption
import com.phew.presentation.write.model.WriteOptions
import com.phew.presentation.write.screen.component.FilteredImageGrid
import com.phew.presentation.write.screen.component.FontSelectorGrid
import com.phew.presentation.write.viewmodel.WriteViewModel
import com.phew.presentation.write.R as WriteR

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
    
    // 완료 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.writeCompleteEvent.collect {
            // TODO: Show Toast and navigate to Feed Home
            onBackPressed() // 임시로 뒤로가기
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    WriteScreen(
        modifier = modifier,
        content = uiState.content,
        tags = uiState.tags,
        currentTagInput = uiState.currentTagInput,
        relatedTags = uiState.relatedNumberTags,
        isWriteCompleted = uiState.canComplete,
        activeBackgroundImageResId = uiState.activeBackgroundResId,
        activeBackgroundUri = uiState.activeBackgroundUri,
        selectedBackgroundFilter = uiState.selectedBackgroundFilter,
        selectedGridImageResId = uiState.selectedGridImageResId,
        selectedFont = uiState.selectedFont,
        selectedFontFamily = uiState.selectedFontFamily,
        selectedOptionIds = uiState.selectedOptionIds,
        hasLocationPermission = uiState.hasLocationPermission,
        showLocationPermissionDialog = uiState.showLocationPermissionDialog,
        showCameraPermissionDialog = uiState.showCameraPermissionDialog,
        showGalleryPermissionDialog = uiState.showGalleryPermissionDialog,
        onBackPressed = onBackPressed,
        onContentChange = viewModel::updateContent,
        onTagInputChange = viewModel::updateTagInput,
        onFilterChange = {
            viewModel.selectBackgroundFilter(it)
            viewModel.hideRelatedTags()
        },
        onImageSelected = {
            viewModel.selectBackgroundImage(it)
            viewModel.hideRelatedTags()
        },
        onCustomImageSelected = {
            viewModel.onBackgroundAlbumImagePicked(it)
            viewModel.hideRelatedTags()
        },
        onContentClick = viewModel::hideRelatedTags, // Add this line
        onFontSelected = {
            viewModel.selectFont(it)
            viewModel.hideRelatedTags()
        },
        onOptionSelected = {
            viewModel.selectOption(it)
            viewModel.hideRelatedTags()
        },
        onDistanceOptionWithoutPermission = viewModel::onDistanceOptionClickWithoutPermission,
        onDismissLocationDialog = viewModel::dismissLocationPermissionDialog,
        onRequestLocationPermission = viewModel::requestLocationPermission,
        onCameraPermissionDenied = viewModel::onCameraPermissionDenied,
        onGalleryPermissionDenied = viewModel::onGalleryPermissionDenied,
        onDismissCameraDialog = viewModel::dismissCameraPermissionDialog,
        onDismissGalleryDialog = viewModel::dismissGalleryPermissionDialog,
        onRequestCameraPermissionFromSettings = viewModel::requestCameraPermissionFromSettings,
        onRequestGalleryPermissionFromSettings = viewModel::requestGalleryPermissionFromSettings,
        onAddTag = viewModel::addTag,
        onRemoveTag = viewModel::removeTag,
        onRelatedTagClick = { tagItem -> viewModel.addTag(tagItem.name) },
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
        onCameraCaptureResult = viewModel::onBackgroundCameraCaptureResult,
        onGallerySettingsResult = viewModel::onGallerySettingsResult,
        onCameraSettingsResult = viewModel::onCameraSettingsResult
    )
}

@Composable
private fun WriteScreen(
    modifier: Modifier = Modifier,
    content: String,
    tags: List<String>,
    currentTagInput: String,
    relatedTags: List<NumberTagItem>,
    isWriteCompleted: Boolean,
    activeBackgroundImageResId: Int?,
    activeBackgroundUri: Uri?,
    selectedBackgroundFilter: String,
    selectedGridImageResId: Int?,
    selectedFont: String,
    selectedFontFamily: FontFamily?,
    selectedOptionIds: List<String>,
    hasLocationPermission: Boolean,
    showLocationPermissionDialog: Boolean,
    showCameraPermissionDialog: Boolean,
    showGalleryPermissionDialog: Boolean,
    onBackPressed: () -> Unit,
    onContentChange: (String) -> Unit,
    onTagInputChange: (String) -> Unit,
    onFilterChange: (filter: String) -> Unit,
    onImageSelected: (Int) -> Unit,
    onCustomImageSelected: (Uri) -> Unit,
    onContentClick: () -> Unit, // Add this line
    onFontSelected: (FontFamily) -> Unit,
    onOptionSelected: (String) -> Unit,
    onDistanceOptionWithoutPermission: () -> Unit,
    onDismissLocationDialog: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onCameraPermissionDenied: () -> Unit,
    onGalleryPermissionDenied: () -> Unit,
    onDismissCameraDialog: () -> Unit,
    onDismissGalleryDialog: () -> Unit,
    onRequestCameraPermissionFromSettings: () -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onRelatedTagClick: (NumberTagItem) -> Unit,
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
    onCameraCaptureResult: (Boolean, Uri) -> Unit,
    onRequestGalleryPermissionFromSettings: () -> Unit,
    onGallerySettingsResult: (Boolean) -> Unit,
    onCameraSettingsResult: (Boolean) -> Unit
) {

    val snackBarHostState = remember { SnackbarHostState() }
    val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
    val albumPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val context = LocalContext.current
    var settingsTarget by remember { mutableStateOf<SettingsTarget?>(null) }
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        when (settingsTarget) {
            SettingsTarget.Camera -> {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                onCameraSettingsResult(granted)
            }

            SettingsTarget.Gallery -> {
                val granted = isGalleryPermissionGranted(context)
                onGallerySettingsResult(granted)
            }

            null -> Unit
        }
        settingsTarget = null
    }

    val cropLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract(),
        onResult = { result ->
            val cropped = result.uriContent ?: return@rememberLauncherForActivityResult
            onCustomImageSelected(cropped)
        }
    )

    CameraPickerEffect(
        effectState = CameraPickerEffectState(
            launchAlbum = shouldLaunchAlbum,
            requestCameraPermission = shouldRequestCameraPermission,
            pendingCapture = pendingCameraCapture
        ),
        onAlbumRequestConsumed = onAlbumRequestConsumed,
        onAlbumPicked = { uri ->
            cropLauncher.launch(
                CropImageContractOptions(
                    uri = uri,
                    cropImageOptions = CropImageOptions()
                )
            )
        },
        onCameraPermissionRequestConsumed = onCameraPermissionRequestConsumed,
        onCameraPermissionResult = onCameraPermissionResult,
        onCameraCaptureLaunched = onCameraCaptureLaunched,
        onCameraCaptureResult = onCameraCaptureResult,
        cameraPermissions = cameraPermissions,
        albumPermissions = albumPermissions,
        onCameraPermissionDenied = onCameraPermissionDenied,
        onGalleryPermissionDenied = onGalleryPermissionDenied
    )

    Scaffold (
        modifier = modifier,
        topBar = {
            AppBar.TextButtonAppBar(
                appBarText = stringResource(WriteR.string.write_screen_title),
                buttonText = stringResource(WriteR.string.write_screen_complete),
                onButtonClick = onWriteComplete,
                onClick = onBackPressed,
                buttonTextColor = if (isWriteCompleted) NeutralColor.BLACK else NeutralColor.GRAY_300
            )
        },
        bottomBar = {
            OptionButtons(
                options = WriteOptions.availableOptions,
                selectedOptionIds = selectedOptionIds,
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
                .windowInsetsPadding(WindowInsets.ime.only(WindowInsetsSides.Bottom))
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(scrollState)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                        placeholder = stringResource(com.phew.core_design.R.string.write_card_content_placeholder),
                        onContentChange = onContentChange,
                        onContentClick = onContentClick, // Add this line
                        onAddTag = onAddTag,
                        onRemoveTag = onRemoveTag,
                        shouldFocusTagInput = focusTagInput,
                        onTagFocusHandled = onTagFocusHandled,
                        currentTagInput = currentTagInput,
                        onTagInputChange = onTagInputChange
                    )
                )

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

            // 관련 태그를 키보드 위에 플로팅 표시
            if (relatedTags.isNotEmpty()) {
                NumberTagFlowLayout(
                    modifier = Modifier.fillMaxWidth(),
                    tags = relatedTags,
                    onTagClick = onRelatedTagClick
                )
            }
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

    if (showCameraPermissionDialog) {
        DialogComponent.DefaultButtonTwo(
            title = stringResource(R.string.camera_permission_title),
            description = stringResource(R.string.camera_permission_description),
            buttonTextStart = stringResource(com.phew.core_design.R.string.permission_settings_negative),
            buttonTextEnd = stringResource(com.phew.core_design.R.string.permission_settings_positive),
            onClick = {
                onRequestCameraPermissionFromSettings()
                settingsTarget = SettingsTarget.Camera
                settingsLauncher.launch(appSettingsIntent(context))
                onDismissCameraDialog()
            },
            onDismiss = onDismissCameraDialog
        )
    }

    if (showGalleryPermissionDialog) {
        DialogComponent.DefaultButtonTwo(
            title = stringResource(R.string.gallery_permission_title),
            description = stringResource(R.string.gallery_permission_description),
            buttonTextStart = stringResource(com.phew.core_design.R.string.permission_settings_negative),
            buttonTextEnd = stringResource(com.phew.core_design.R.string.permission_settings_positive),
            onClick = {
                onRequestGalleryPermissionFromSettings()
                settingsTarget = SettingsTarget.Gallery
                settingsLauncher.launch(appSettingsIntent(context))
                onDismissGalleryDialog()
            },
            onDismiss = onDismissGalleryDialog
        )
    }

    CameraPickerBottomSheet(
        visible = showBackgroundPicker,
        onActionSelected = onCameraPickerAction,
        onDismiss = onCameraPickerDismissed
    )
}

private fun appSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }

private fun isGalleryPermissionGranted(context: Context): Boolean {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

private enum class SettingsTarget {
    Camera,
    Gallery
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
    selectedOptionIds: List<String>,
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
                RoundButton(
                    text = option.displayName,
                    selected = selectedOptionIds.contains(option.id),
                    onClick = {
                        if (isDistanceOption && !hasLocationPermission) {
                            onDistancePermissionRequest()
                        } else {
                            onOptionSelected(option)
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoundButtonPreview() {
    var selectedIds by remember { mutableStateOf(listOf(WriteOptions.availableOptions.first().id)) }

    OptionButtons(
        options = WriteOptions.availableOptions,
        selectedOptionIds = selectedIds,
        hasLocationPermission = true,
        onOptionSelected = { option ->
            selectedIds = if (selectedIds.contains(option.id)) {
                selectedIds.filter { it != option.id }
            } else {
                selectedIds + option.id
            }
        },
        onDistancePermissionRequest = {}
    )
}


private const val TAG = "WriteScreen"
