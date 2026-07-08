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
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
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
import com.phew.presentation.write.model.WriteUiState
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
import com.phew.presentation.write.screen.component.PollCreateScreen
import com.phew.presentation.write.screen.component.PollOptionUi
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

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val defaultContent = stringResource(WriteR.string.write_card_content_default_placeholder)
    var activeDialog by remember { mutableStateOf<WriteDialog?>(null) }

    WriteRouteEffects(
        viewModel = viewModel,
        navController = navController,
        args = args,
        isFromTab = isFromTab,
        onWriteComplete = onWriteComplete,
        onDialogRequested = { activeDialog = it },
    )

    WriteScreen(
        modifier = modifier,
        args = args,
        uiState = uiState,
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

            if (uiState.content == defaultContent) {
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
        onCompleteTagInput = viewModel::completeTagInput,
        onResetTagInput = viewModel::resetTagInput,
        onTagFocusHandled = viewModel::onTagInputFocusHandled,
        onWriteComplete = viewModel::onWriteComplete,
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
        onEnterClick = {
            viewModel.writeFinishTagEnter(isFromFeedCard = isFromTab)
        },
        onPollClose = viewModel::closePollCreate,
        onPollComplete = viewModel::completePollCreate,
        onPollOptionChange = viewModel::updateDraftPollOption,
        onPollAddOption = viewModel::addDraftPollOption,
        onPollRemoveOption = viewModel::removeDraftPollOption,
        onPollEdit = viewModel::openPollCreate,
        onPollDelete = viewModel::deletePoll,
    )

    WriteDialogHost(
        dialog = activeDialog,
        activateDate = uiState.activateDate,
        onDismissAndGoHome = {
            activeDialog = null
            onHome()
        },
        onBadImageDismissed = {
            activeDialog = null
            viewModel.resetToDefaultImage()
        },
    )
}

private sealed interface WriteDialog {
    data class Error(val refreshToken: String) : WriteDialog
    data object Restricted : WriteDialog
    data object Deleted : WriteDialog
    data object BadImage : WriteDialog
}

@Composable
private fun WriteRouteEffects(
    viewModel: WriteViewModel,
    navController: NavController,
    args: WriteArgs?,
    isFromTab: Boolean,
    onWriteComplete: (CardDetailArgs) -> Unit,
    onDialogRequested: (WriteDialog) -> Unit,
) {
    val context = LocalContext.current
    val currentOnWriteComplete by rememberUpdatedState(onWriteComplete)
    val currentOnDialogRequested by rememberUpdatedState(onDialogRequested)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { result ->
            viewModel.onLocationPermissionResult(result.values.any { it })
        },
    )

    LaunchedEffect(viewModel, isFromTab) {
        if (isFromTab) viewModel.isComeFromTab()
    }

    LaunchedEffect(context, viewModel) {
        viewModel.onInitialLocationPermissionCheck(
            isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                isPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION),
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.requestPermissionEvent.collect { permissions ->
            locationPermissionLauncher.launch(permissions)
        }
    }

    LaunchedEffect(viewModel, navController) {
        viewModel.writeCompleteEvent.collect { cardId ->
            SooumLog.d(TAG, "writeCompleteEvent")
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("card_added", true)
            currentOnWriteComplete(CardDetailArgs(cardId = cardId))
        }
    }

    LaunchedEffect(viewModel, args?.parentCardId) {
        args?.parentCardId?.let(viewModel::setParentCardId)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            currentOnDialogRequested(effect.toRouteDialog())
        }
    }
}

private fun WriteUiEffect.toRouteDialog(): WriteDialog = when (this) {
    is WriteUiEffect.ShowError -> WriteDialog.Error(refreshToken)
    WriteUiEffect.ShowRestricted -> WriteDialog.Restricted
    WriteUiEffect.ShowDeleted -> WriteDialog.Deleted
    WriteUiEffect.ShowBadImage -> WriteDialog.BadImage
}

private fun isPermissionGranted(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

@Composable
private fun WriteDialogHost(
    dialog: WriteDialog?,
    activateDate: UiState<String>,
    onDismissAndGoHome: () -> Unit,
    onBadImageDismissed: () -> Unit,
) {
    when (dialog) {
        null -> Unit
        WriteDialog.Restricted -> RestrictedWriteDialog(
            activateDate = activateDate,
            onDismiss = onDismissAndGoHome,
        )
        WriteDialog.Deleted -> DeletedCardDialog(
            onConfirm = onDismissAndGoHome,
            onDismiss = onDismissAndGoHome,
        )
        WriteDialog.BadImage -> DialogComponent.DefaultButtonOne(
            title = stringResource(WriteR.string.write_screen_picture_dialog_image_title),
            description = stringResource(WriteR.string.write_screen_picture_dialog_image_content),
            buttonText = stringResource(com.phew.core_design.R.string.common_okay),
            onClick = onBadImageDismissed,
            onDismiss = onBadImageDismissed,
        )
        is WriteDialog.Error -> {
            val keyboard = LocalSoftwareKeyboardController.current
            val focusManager = LocalFocusManager.current
            ErrorDialog(
                onDismiss = {
                    keyboard?.hide()
                    focusManager.clearFocus(force = true)
                    onDismissAndGoHome()
                },
                refreshToken = dialog.refreshToken,
            )
        }
    }
}

@Composable
private fun RestrictedWriteDialog(
    activateDate: UiState<String>,
    onDismiss: () -> Unit,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.phew.core_design.R.raw.ic_refresh),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = activateDate is UiState.Loading,
    )

    when (activateDate) {
        is UiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(44.dp),
            )
        }
        is UiState.Success, is UiState.Fail -> {
            val date = (activateDate as? UiState.Success)?.data.orEmpty()
            DialogComponent.DefaultButtonOne(
                title = stringResource(WriteR.string.write_screen_dialog_restrict_title),
                description = stringResource(
                    WriteR.string.write_screen_dialog_restrict_message,
                    date,
                ),
                onClick = onDismiss,
                onDismiss = onDismiss,
                buttonText = stringResource(com.phew.core_design.R.string.common_okay),
            )
        }
    }
}

@Composable
private fun WriteScreen(
    modifier: Modifier = Modifier,
    args: WriteArgs? = null,
    uiState: WriteUiState,
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
    onCompleteTagInput: () -> Unit,
    onResetTagInput: () -> Unit,
    onTagFocusHandled: () -> Unit,
    onWriteComplete: () -> Unit,
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
    onEnterClick: () -> Unit,
    onPollClose: () -> Unit,
    onPollComplete: () -> Unit,
    onPollOptionChange: (Long, String) -> Unit,
    onPollAddOption: () -> Unit,
    onPollRemoveOption: (Long) -> Unit,
    onPollEdit: () -> Unit,
    onPollDelete: () -> Unit,
) {
    if (uiState.isPollCreateMode) {
        PollCreateScreen(
            modifier = modifier.fillMaxSize(),
            options = uiState.draftPollContents.mapIndexed { index, text ->
                PollOptionUi(id = index.toLong(), text = text)
            },
            onOptionChange = onPollOptionChange,
            onAddOption = onPollAddOption,
            onRemoveOption = onPollRemoveOption,
            onClose = onPollClose,
            onComplete = onPollComplete,
        )
        return
    }

    val content = uiState.content
    val tags = uiState.tags
    val currentTagInput = uiState.currentTagInput
    val relatedTags = uiState.relatedNumberTags
    val isWriteCompleted = uiState.canComplete
    val activeBackgroundImageResId = uiState.activeBackgroundResId
    val activeBackgroundUri = uiState.activeBackgroundUri
    val selectedBackgroundFilter = uiState.selectedBackgroundFilter
    val selectedGridImageName = uiState.selectedGridImageName
    val selectedFont = uiState.selectedFont
    val selectedFontType = uiState.selectedFontType
    val selectedOptionIds = uiState.selectedOptionIds
    val hasLocationPermission = uiState.hasLocationPermission
    val cardDefaultImagesByCategory = uiState.cardDefaultImagesByCategory

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
            launchAlbum = uiState.shouldLaunchBackgroundAlbum,
            requestCameraPermission = uiState.shouldRequestBackgroundCameraPermission,
            pendingCapture = uiState.pendingBackgroundCameraCapture,
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
                                shouldFocusTagInput = uiState.focusTagInput,
                                onTagFocusHandled = onTagFocusHandled,
                                currentTagInput = currentTagInput,
                                onTagInputChange = onTagInputChange,
                                enterClick = onEnterClick
                            )
                        )
                    }

                    if (uiState.pollContents.isNotEmpty()) {
                        PollPreviewCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            pollContents = uiState.pollContents,
                            onEdit = onPollEdit,
                            onDelete = onPollDelete,
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
                    WriteOptions.availableOptions.filter {
                        it.id != "twenty_four_hours" && it.id != WriteOptions.POLL_OPTION_ID
                    }
                } else {
                    WriteOptions.availableOptions
                }
                val displayedSelectedOptionIds = if (uiState.pollContents.isNotEmpty()) {
                    selectedOptionIds + WriteOptions.POLL_OPTION_ID
                } else {
                    selectedOptionIds
                }

                if (showOptionButtons) {
                    OptionButtons(
                        options = filteredOptions,
                        selectedOptionIds = displayedSelectedOptionIds,
                        hasLocationPermission = hasLocationPermission,
                        onOptionSelected = { option -> onOptionSelected(option.id) },
                        onDistancePermissionRequest = onDistanceOptionWithoutPermission
                    )
                }
            }
        }

        if (uiState.isWriteInProgress) {
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

    if (uiState.showLocationPermissionDialog) {
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

    if (uiState.showCameraPermissionDialog) {
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
    if (uiState.showGalleryPermissionDialog) {
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
        visible = uiState.showBackgroundPickerSheet,
        onActionSelected = onCameraPickerAction,
        onDismiss = onCameraPickerDismissed
    )
}

private fun appSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }

@Composable
private fun PollPreviewCard(
    pollContents: List<String>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onEdit)
                    .padding(8.dp),
                painter = painterResource(com.phew.core_design.R.drawable.ic_write_stoke),
                contentDescription = stringResource(WriteR.string.write_poll_edit),
                tint = NeutralColor.GRAY_500,
            )
            Icon(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onDelete)
                    .padding(8.dp),
                painter = painterResource(com.phew.core_design.R.drawable.ic_delete),
                contentDescription = stringResource(WriteR.string.write_poll_delete),
                tint = NeutralColor.GRAY_500,
            )
        }
        pollContents.forEach { content ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        color = NeutralColor.GRAY_100,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = content,
                    style = TextComponent.SUBTITLE_1_M_16,
                    color = NeutralColor.BLACK,
                )
            }
        }
    }
}

private fun isGalleryPermissionGranted(context: Context): Boolean {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return isPermissionGranted(context, permission)
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
                    iconResId = option.iconResId(),
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

private fun WriteOption.iconResId(): Int? = when (id) {
    WriteOptions.DISTANCE_OPTION_ID -> com.phew.core_design.R.drawable.ic_location_stoke
    "twenty_four_hours" -> com.phew.core_design.R.drawable.ic_timer_stoke
    WriteOptions.POLL_OPTION_ID -> com.phew.core_design.R.drawable.ic_vote_stoke
    else -> null
}


private const val TAG = "WriteScreen"
