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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import com.phew.core_design.typography.FontType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phew.core.ui.R
import com.phew.core.ui.component.ErrorDialog
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
import com.phew.core_design.LoadingAnimation
import com.phew.core_design.component.button.RoundButton
import com.phew.core_design.component.card.BaseCardData
import com.phew.core_design.component.card.CardView
import com.phew.presentation.write.component.NumberTagFlowLayout
import com.phew.presentation.write.component.NumberTagItem
import com.phew.domain.dto.CardImageDefault
import com.phew.presentation.write.model.BackgroundConfig
import com.phew.core_design.FontItem
import com.phew.presentation.write.model.WriteOption
import com.phew.presentation.write.model.WriteOptions
import com.phew.presentation.write.screen.component.FontSelectorGrid
import com.phew.presentation.write.viewmodel.WriteViewModel
import com.phew.core.ui.model.navigation.WriteArgs
import com.phew.core_design.CustomFont
import com.phew.core_design.component.filter.SooumFilter
import com.phew.presentation.write.model.BackgroundFilterType
import com.phew.presentation.write.screen.component.ImageGrid
import com.phew.presentation.write.R as WriteR
import androidx.navigation.NavController
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core_common.log.SooumLog
import com.phew.core_design.DialogComponent.DeletedCardDialog
import com.phew.presentation.write.viewmodel.WriteUiEffect
import com.phew.presentation.write.viewmodel.UiState
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
internal fun WriteRoute(
    modifier: Modifier = Modifier,
    navController: NavController,
    args: WriteArgs? = null,
    viewModel: WriteViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onWriteComplete: (CardDetailArgs) -> Unit,
    onHome: () -> Unit,
    isFromTab: Boolean = false,
) {
    BackHandler {
        onBackPressed()
        viewModel.clickBackHandler(isFromFeedCard = isFromTab)
    }

    LaunchedEffect(Unit) {
        if (isFromTab) viewModel.isComeFromTab()
    }
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

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
            SooumLog.d(TAG, "writeCompleteEvent")
            navController.previousBackStackEntry?.savedStateHandle?.set("card_added", true)
            onWriteComplete(CardDetailArgs(cardId = it))
        }
    }

    // parentCardId 설정
    LaunchedEffect(args) {
        args?.parentCardId?.let { parentCardId ->
            viewModel.setParentCardId(parentCardId)
        }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val compareContent = stringResource(WriteR.string.write_card_content_default_placeholder)
    var errorWithRefreshToken by remember { mutableStateOf<String?>(null) }
    var showRestrictedDialog by remember { mutableStateOf(false) }
    var showDeletedDialog by remember { mutableStateOf(false) }
    var showBadImageDialog by remember { mutableStateOf(false) }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.phew.core_design.R.raw.ic_refresh)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = showRestrictedDialog && uiState.activateDate is UiState.Loading
    )

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is WriteUiEffect.ShowError -> {
                    errorWithRefreshToken = effect.refreshToken
                }

                WriteUiEffect.ShowRestricted -> {
                    showRestrictedDialog = true
                }

                WriteUiEffect.ShowDeleted -> {
                    showDeletedDialog = true
                }

                WriteUiEffect.ShowBadImage -> {
                    showBadImageDialog = true
                }
            }
        }
    }

    WriteScreen(
        modifier = modifier,
        args = args,
        content = uiState.content,
        tags = uiState.tags,
        currentTagInput = uiState.currentTagInput,
        relatedTags = uiState.relatedNumberTags,
        isWriteCompleted = uiState.canComplete,
        activeBackgroundImageResId = uiState.activeBackgroundResId,
        activeBackgroundUri = uiState.activeBackgroundUri,
        selectedBackgroundFilter = uiState.selectedBackgroundFilter,
        selectedGridImageName = uiState.selectedGridImageName,
        selectedFont = uiState.selectedFont,
        selectedFontType = uiState.selectedFontType,
        selectedOptionIds = uiState.selectedOptionIds,
        hasLocationPermission = uiState.hasLocationPermission,
        showLocationPermissionDialog = uiState.showLocationPermissionDialog,
        showCameraPermissionDialog = uiState.showCameraPermissionDialog,
        showGalleryPermissionDialog = uiState.showGalleryPermissionDialog,
        cardDefaultImagesByCategory = uiState.cardDefaultImagesByCategory,
        onBackPressed = onBackPressed,
        onContentChange = viewModel::updateContent,
        onTagInputChange = viewModel::updateTagInput,
        onFilterChange = {
            viewModel.selectBackgroundFilter(it, isFromTab)
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
        onContentClick = {
            viewModel.hideRelatedTags()

            if (uiState.content == compareContent) {
                viewModel.updateContent("")
            }
        },
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
        onCompleteTagInput = viewModel::completeTagInput,
        onResetTagInput = viewModel::resetTagInput,
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
        onCameraSettingsResult = viewModel::onCameraSettingsResult,
        hideRelatedTags = viewModel::hideRelatedTags,
        isWriteInProgress = uiState.isWriteInProgress,
        onEnterClick = {
            viewModel.writeFinishTagEnter(isFromFeedCard = isFromTab)
        }
    )

    if (showRestrictedDialog) {
        when (val activateDate = uiState.activateDate) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            is UiState.Success, is UiState.Fail -> {
                val dateString = if (activateDate is UiState.Success) activateDate.data else ""
                val onDismissAction = {
                    showRestrictedDialog = false
                    onHome()
                }
                DialogComponent.DefaultButtonOne(
                    title = stringResource(WriteR.string.write_screen_dialog_restrict_title),
                    description = stringResource(
                        WriteR.string.write_screen_dialog_restrict_message,
                        dateString
                    ),
                    onClick = onDismissAction,
                    onDismiss = onDismissAction,
                    buttonText = stringResource(com.phew.core_design.R.string.common_okay)
                )
            }
        }
    }

    if (showDeletedDialog) {
        DeletedCardDialog(
            onConfirm = {
                showDeletedDialog = false
                onHome()
            },
            onDismiss = {
                showDeletedDialog = false
                onHome()
            }
        )
    }

    if (showBadImageDialog) {
        DialogComponent.DefaultButtonOne(
            title = stringResource(WriteR.string.write_screen_picture_dialog_image_title),
            description = stringResource(WriteR.string.write_screen_picture_dialog_image_content),
            buttonText = stringResource(com.phew.core_design.R.string.common_okay),
            onClick = {
                showBadImageDialog = false
                viewModel.resetToDefaultImage()
            },
            onDismiss = {
                showBadImageDialog = false
                viewModel.resetToDefaultImage()
            }
        )
    }

    errorWithRefreshToken?.let { refreshToken ->
        ErrorDialog(
            onDismiss = {
                keyboard?.hide()
                focusManager.clearFocus(force = true)
                errorWithRefreshToken = null
                onHome()
            },
            refreshToken = refreshToken
        )
    }
}

@Composable
private fun WriteScreen(
    modifier: Modifier = Modifier,
    args: WriteArgs? = null,
    content: String,
    tags: List<String>,
    currentTagInput: String,
    relatedTags: List<NumberTagItem>,
    isWriteCompleted: Boolean,
    activeBackgroundImageResId: Int?,
    activeBackgroundUri: Uri?,
    selectedBackgroundFilter: BackgroundFilterType,
    selectedGridImageName: String?,
    selectedFont: String,
    selectedFontType: FontType?,
    selectedOptionIds: List<String>,
    hasLocationPermission: Boolean,
    showLocationPermissionDialog: Boolean,
    showCameraPermissionDialog: Boolean,
    showGalleryPermissionDialog: Boolean,
    cardDefaultImagesByCategory: Map<BackgroundFilterType, List<CardImageDefault>>,
    onBackPressed: () -> Unit,
    onContentChange: (String) -> Unit,
    onTagInputChange: (String) -> Unit,
    onFilterChange: (filter: BackgroundFilterType) -> Unit,
    onImageSelected: (String) -> Unit,
    onCustomImageSelected: (Uri) -> Unit,
    onContentClick: () -> Unit,
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
    onCompleteTagInput: () -> Unit,
    onResetTagInput: () -> Unit,
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
    onCameraSettingsResult: (Boolean) -> Unit,
    hideRelatedTags: () -> Unit,
    isWriteInProgress: Boolean,
    onEnterClick: () -> Unit,
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
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
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

    var isCardFocused by remember { mutableStateOf(false) }
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    var lastImeVisible by remember { mutableStateOf(isImeVisible) }
    val finalizeTagInputIfNeeded: () -> Unit = {
        if (currentTagInput.isNotBlank()) {
            onCompleteTagInput()
        } else {
            onResetTagInput()
        }
    }

    LaunchedEffect(isImeVisible) {
        if (!isImeVisible && lastImeVisible) {
            hideRelatedTags()
            finalizeTagInputIfNeeded()
        }
        lastImeVisible = isImeVisible
    }

    LaunchedEffect(isCardFocused) {
        if (isCardFocused) {
            hideRelatedTags()
            finalizeTagInputIfNeeded()
            isCardFocused = false
        }
    }

    CameraPickerEffect(
        effectState = CameraPickerEffectState(
            launchAlbum = shouldLaunchAlbum,
            requestCameraPermission = shouldRequestCameraPermission,
            pendingCapture = pendingCameraCapture
        ),
        onAlbumRequestConsumed = onAlbumRequestConsumed,
        onAlbumPicked = onCustomImageSelected,
        onCameraPermissionRequestConsumed = onCameraPermissionRequestConsumed,
        onCameraPermissionResult = onCameraPermissionResult,
        onCameraCaptureLaunched = onCameraCaptureLaunched,
        onCameraCaptureResult = onCameraCaptureResult,
        cameraPermissions = cameraPermissions,
        albumPermissions = albumPermissions,
        onCameraPermissionDenied = onCameraPermissionDenied,
        onGalleryPermissionDenied = onGalleryPermissionDenied
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                keyboard?.hide()
                focusManager.clearFocus()
                hideRelatedTags()
            },
            topBar = {
                val titleRes = if (args?.parentCardId != null) {
                    WriteR.string.write_screen_comment_title
                } else {
                    WriteR.string.write_screen_title
                }
                AppBar.TextButtonAppBarText(
                    appBarText = stringResource(titleRes),
                    buttonText = stringResource(WriteR.string.write_screen_complete),
                    onButtonClick = {
                        // 입력 중인 태그가 있으면 먼저 추가
                        if (currentTagInput.isNotBlank()) {
                            onAddTag(currentTagInput)
                            onResetTagInput()
                        }
                        onWriteComplete()
                    },
                    onClick = onBackPressed,
                    buttonTextColor = if (isWriteCompleted) NeutralColor.BLACK else NeutralColor.GRAY_300
                )
            },
            snackbarHost = {
                DialogComponent.CustomAnimationSnackBarHost(snackBarHostState)
            }
        ) { innerPadding ->
            val scrollState = rememberScrollState()
            var isUserDragging by remember { mutableStateOf(false) }
            LaunchedEffect(scrollState.isScrollInProgress, isUserDragging) {
                if (scrollState.isScrollInProgress && isUserDragging) {
                    finalizeTagInputIfNeeded()
                    hideRelatedTags()
                    keyboard?.hide()
                    focusManager.clearFocus()
                }
            }
            val layoutDirection = LocalLayoutDirection.current

            Column(
                modifier = Modifier
                    .background(NeutralColor.WHITE)
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection)
                    )
                    .windowInsetsPadding(
                        WindowInsets.ime.union(WindowInsets.navigationBars)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .pointerInput(isImeVisible) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (!isImeVisible) continue
                                    val dragDetected = event.changes.any { pointer ->
                                        pointer.type == PointerType.Touch &&
                                                pointer.pressed &&
                                                !pointer.isConsumed &&
                                                pointer.positionChange() != Offset.Zero
                                    }
                                    if (dragDetected) {
                                        isUserDragging = true
                                    }
                                    if (!event.changes.any { it.pressed }) {
                                        isUserDragging = false
                                    }
                                }
                            }
                        }
                        .verticalScroll(scrollState)
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CardView(
                            modifier = Modifier,
                            data = BaseCardData.Write(
                                content = content,
                                tags = tags,
                                backgroundResId = activeBackgroundImageResId,
                                backgroundUri = activeBackgroundUri,
                                fontType = selectedFontType,
                                placeholder = stringResource(WriteR.string.write_card_content_default_placeholder),
                                onContentChange = onContentChange,
                                onContentClick = {
                                    onContentClick()
                                    isCardFocused = true
                                },
                                onAddTag = onAddTag,
                                onRemoveTag = onRemoveTag,
                                shouldFocusTagInput = focusTagInput,
                                onTagFocusHandled = onTagFocusHandled,
                                currentTagInput = currentTagInput,
                                onTagInputChange = onTagInputChange,
                                enterClick = onEnterClick
                            )
                        )
                    }

                    BackgroundSelect(
                        modifier = Modifier.fillMaxWidth(),
                        selectedGridImageName = selectedGridImageName,
                        selectedBackgroundFilter = selectedBackgroundFilter,
                        cardDefaultImagesByCategory = cardDefaultImagesByCategory,
                        onFilterChange = onFilterChange,
                        onImageSelected = onImageSelected,
                        onCameraClick = onCameraPickerRequested
                    )

                    FontSelect(
                        fontItem = CustomFont.fontData,
                        selectedFont = selectedFont,
                        onFontSelected = onFontSelected
                    )
                }

                val showRelatedTags = relatedTags.isNotEmpty() && isImeVisible
                val showOptionButtons = relatedTags.isEmpty() && !isImeVisible
                val density = LocalDensity.current
                LaunchedEffect(showRelatedTags) {
                    if (showRelatedTags) {
                        val shift = with(density) { 30.dp.toPx() }
                        scrollState.animateScrollBy(shift)
                    }
                }

                if (showRelatedTags) {
                    NumberTagFlowLayout(
                        modifier = Modifier.fillMaxWidth(),
                        tags = relatedTags,
                        onTagClick = {
                            onRelatedTagClick(it)
                            hideRelatedTags() // Hide immediately on click
                        }
                    )
                }

                val filteredOptions = if (args?.parentCardId != null) {
                    WriteOptions.availableOptions.filter { it.id != "twenty_four_hours" }
                } else {
                    WriteOptions.availableOptions
                }

                if (showOptionButtons) {
                    OptionButtons(
                        options = filteredOptions,
                        selectedOptionIds = selectedOptionIds,
                        hasLocationPermission = hasLocationPermission,
                        onOptionSelected = { option -> onOptionSelected(option.id) },
                        onDistancePermissionRequest = onDistanceOptionWithoutPermission
                    )
                }
            }
        }

        if (isWriteInProgress) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation.LoadingView(
                    modifier = Modifier.fillMaxWidth()
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
            onDismiss = onDismissLocationDialog,
            startButtonTextColor = NeutralColor.GRAY_600
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
            onDismiss = onDismissCameraDialog,
            startButtonTextColor = NeutralColor.GRAY_600
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
            onDismiss = onDismissGalleryDialog,
            startButtonTextColor = NeutralColor.GRAY_600
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
    return ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

private enum class SettingsTarget {
    Camera,
    Gallery
}

@Composable
private fun BackgroundSelect(
    modifier: Modifier,
    selectedGridImageName: String?,
    selectedBackgroundFilter: BackgroundFilterType,
    cardDefaultImagesByCategory: Map<BackgroundFilterType, List<CardImageDefault>>,
    onFilterChange: (filter: BackgroundFilterType) -> Unit,
    onImageSelected: (String) -> Unit,
    onCameraClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Text(
            text = stringResource(com.phew.presentation.write.R.string.write_screen_background_section),
            style = TextComponent.CAPTION_1_SB_12.copy(color = Primary.DARK),
        )

        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            SooumFilter(
                modifier = Modifier.fillMaxWidth(),
                selectedFilter = selectedBackgroundFilter,
                filters = BackgroundConfig.filterTypes,
                onFilterSelected = onFilterChange,
                labelProvider = { filterType ->
                    stringResource(filterType.getStringRes())
                }
            )

            val currentFilterImages =
                remember(selectedBackgroundFilter, cardDefaultImagesByCategory) {
                    cardDefaultImagesByCategory[selectedBackgroundFilter] ?: emptyList()
                }

            ImageGrid(
                cardDefaultImages = currentFilterImages,
                selectedImageName = selectedGridImageName,
                onImageClick = { imageName ->
                    onImageSelected(imageName)
                },
                onCameraClick = onCameraClick
            )
        }
    }
}

@Composable
private fun FontSelect(
    fontItem: List<FontItem>,
    selectedFont: String,
    onFontSelected: (FontFamily) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)) {
        Text(
            text = stringResource(com.phew.presentation.write.R.string.write_screen_font_section),
            style = TextComponent.CAPTION_1_SB_12.copy(color = Primary.DARK),
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(NeutralColor.WHITE)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(NeutralColor.GRAY_200)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
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
