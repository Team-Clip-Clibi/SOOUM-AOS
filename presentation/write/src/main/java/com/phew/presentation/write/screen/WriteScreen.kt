package com.phew.presentation.write.screen

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phew.core_design.AppBar
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.core_design.component.button.RoundButton
import com.phew.core_design.component.card.BaseCardData
import com.phew.core_design.component.card.CardView
import com.phew.core_design.component.filter.SooumFilter
import com.phew.presentation.write.model.BackgroundConfig
import com.phew.presentation.write.model.FontConfig
import com.phew.presentation.write.model.FontItem
import com.phew.presentation.write.model.WriteOptions
import com.phew.presentation.write.screen.component.FilteredImageGrid
import com.phew.presentation.write.screen.component.FontSelectorGrid
import com.phew.presentation.write.viewmodel.WriteViewModel
import kotlin.collections.orEmpty
import kotlin.io.path.Path

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

    //   위치 권한
    val locationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionResult ->
            val isGranted =
                permissionResult[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            viewModel.onPermissionResult(isGranted = isGranted)
        }
    )
    
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
        selectedImg = uiState.selectedBackgroundImage?.toString() ?: "",
        selectedBackgroundFilter = uiState.selectedBackgroundFilter,
        selectedFont = uiState.selectedFont,
        selectedOption = uiState.selectedOption,
        onBackPressed = onBackPressed,
        onFilterChange = viewModel::selectBackgroundFilter,
        onImageSelected = viewModel::selectBackgroundImage,
        onFontSelected = viewModel::selectFont,
        onOptionSelected = viewModel::selectOption,
        onWriteComplete = viewModel::onWriteComplete
    )
}

@Composable
private fun WriteScreen(
    modifier: Modifier = Modifier,
    content: String,
    tags: List<String>,
    isWriteCompleted: Boolean,
    selectedImg: String,
    selectedBackgroundFilter: String,
    selectedFont: String,
    selectedOption: String,
    onBackPressed: () -> Unit,
    onFilterChange: (filter: String) -> Unit,
    onImageSelected: (Int?) -> Unit,
    onFontSelected: (FontFamily) -> Unit,
    onOptionSelected: (String) -> Unit,
    onWriteComplete: () -> Unit
) {

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
                options = WriteOptions.availableOptions.map { it.displayName },
                selectedOption = selectedOption,
                onOptionSelected = onOptionSelected
            )
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CardView(
                modifier = Modifier.fillMaxWidth(),
                data = BaseCardData.Write(
                    content = content,
                    tags = tags
                )
            )
            
            BackgroundSelect(
                modifier = Modifier.fillMaxWidth(),
                selectedImg = selectedImg,
                selectedBackgroundFilter = selectedBackgroundFilter,
                onFilterChange = onFilterChange,
                onImageSelected = onImageSelected
            )

            FontSelect(
                fontItem = FontConfig.availableFonts,
                selectedFont = selectedFont,
                onFontSelected = onFontSelected
            )
        }
    }
}

@Composable
private fun BackgroundSelect(
    modifier: Modifier,
    selectedImg: String,
    selectedBackgroundFilter: String,
    onFilterChange: (filter: String) -> Unit,
    onImageSelected: (Int?) -> Unit
) {
    var selectedImage by remember { mutableStateOf<Int?>(null) }

    val currentImages by remember(selectedBackgroundFilter) {
        derivedStateOf { BackgroundConfig.imagesByFilter[selectedBackgroundFilter].orEmpty() }
    }

    LaunchedEffect(currentImages) {
        if (currentImages.isNotEmpty()) {
            selectedImage = currentImages.first()
            onImageSelected(selectedImage)
        }
    }

    Column {
        Text(
            text = "배경",
            style = TextComponent.CAPTION_1_SB_12.copy(color = Primary.DARK)
        )

        FilteredImageGrid(
            filters = BackgroundConfig.filterNames,
            imagesByFilter = BackgroundConfig.imagesByFilter,
            selectedFilter = selectedBackgroundFilter,
            selectedImage = selectedImage,
            onFilterSelected = { filter ->
                onFilterChange(filter)
            },
            onImageSelected = { resId ->
                selectedImage = resId
                onImageSelected(resId)
            },
            onCameraClick = {
                selectedImage = null
                onImageSelected(null)
            }
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
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
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
                RoundButton(
                    text = option,
                    selected = option == selectedOption,
                    onClick = { onOptionSelected(option) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoundButtonPreview() {
    var selected by remember { mutableStateOf("거리공유") }

    val options = listOf("거리공유", "24시간")

    OptionButtons(
        options = options,
        selectedOption = selected,
        onOptionSelected = { selected = it }
    )
}


private const val TAG = "WriteScreen"
