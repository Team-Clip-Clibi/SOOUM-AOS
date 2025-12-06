package com.phew.presentation.write.screen.component

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.phew.core_design.NeutralColor
import com.phew.core_design.R
import com.phew.core_design.component.filter.SooumFilter
import com.phew.domain.dto.CardImageDefault


@Composable
internal fun FilteredImageGrid(
    modifier: Modifier = Modifier,
    filters: List<String>,
    selectedFilter: String,
    selectedImageName: String?,
    cardDefaultImagesByCategory: Map<String, List<CardImageDefault>>,
    onFilterSelected: (String) -> Unit,
    onImageSelected: (String) -> Unit,
    onCameraClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        SooumFilter(
            modifier = Modifier.fillMaxWidth(),
            filters = filters,
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected
        )

        // 서버 카테고리를 한국어 필터명으로 매핑
        val categoryMapping = mapOf(
            "COLOR" to "컬러",
            "NATURE" to "자연",
            "SENSITIVITY" to "감성",
            "FOOD" to "푸드",
            "ABSTRACT" to "추상",
            "MEMO" to "메모"
        )

        // 현재 선택된 필터에 해당하는 이미지들
        val currentFilterImages = remember(selectedFilter, cardDefaultImagesByCategory) {
            val serverCategory = categoryMapping.entries.find { it.value == selectedFilter }?.key
            cardDefaultImagesByCategory[serverCategory] ?: emptyList()
        }

        ImageGrid(
            cardDefaultImages = currentFilterImages,
            selectedImageName = selectedImageName,
            onImageClick = { imageName ->
                onImageSelected(imageName)
            },
            onCameraClick = onCameraClick
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ImageGrid(
    cardDefaultImages: List<CardImageDefault>,
    selectedImageName: String?,
    onImageClick: (String) -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4
) {
    val totalItems = 8 // 7개 이미지 + 1개 카메라
    val displayImages = cardDefaultImages.take(totalItems - 1)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
    ) {
        val gridCells = remember(displayImages) {
            val base = displayImages.map { GridCell.Image(it) } + GridCell.Camera
            val remainder = base.size % columns
            if (remainder == 0) {
                base
            } else {
                val padding = List(columns - remainder) { GridCell.Placeholder }
                base + padding
            }
        }

        val containerWidth = maxWidth

        Column(
            Modifier
                .width(containerWidth)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, NeutralColor.GRAY_200, RoundedCornerShape(12.dp))
        ) {
            FixedGrid(
                columns = columns,
                modifier = Modifier.width(containerWidth)
            ) {
                gridCells.forEach { cell ->
                    when (cell) {
                        is GridCell.Image -> {
                            GridImageItem(
                                cardImage = cell.cardImage,
                                isSelected = selectedImageName == cell.cardImage.imageName,
                                onClick = { onImageClick(cell.cardImage.imageName) },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .aspectRatio(1f)
                            )
                        }

                        GridCell.Camera -> {
                            CameraGridItem(
                                onClick = onCameraClick,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .aspectRatio(1f)
                            )
                        }

                        GridCell.Placeholder -> {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun GridImageItem(
    cardImage: CardImageDefault,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    //  TODO 흰색선이 컬러 탭에서만 보임 (앞에 두개)
    Box(
        modifier = modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        AsyncImage(
            model = cardImage.url,
            contentDescription = cardImage.imageName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        if (isSelected) {
            // Black overlay with 30% opacity
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NeutralColor.BLACK.copy(alpha = 0.3f))
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(NeutralColor.WHITE, CircleShape)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_check_round),
                    contentDescription = "선택됨",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun CameraGridItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(NeutralColor.GRAY_100)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_camera_filled),
            contentDescription = "카메라",
            tint = NeutralColor.GRAY_400,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun FixedGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val maxWidth = constraints.maxWidth
        check(maxWidth != Constraints.Infinity) {
            "FixedGrid requires a bounded width."
        }
        val cellWidth = maxWidth / columns
        val cellConstraints = Constraints.fixed(cellWidth, cellWidth)
        val placeables = measurables.map { measurable ->
            measurable.measure(cellConstraints)
        }
        val rows = if (placeables.isEmpty()) 0 else (placeables.size + columns - 1) / columns
        val height = cellWidth * rows

        layout(maxWidth, height) {
            placeables.forEachIndexed { index, placeable ->
                val row = index / columns
                val column = index % columns
                val x = column * cellWidth
                val y = row * cellWidth
                placeable.placeRelative(x, y)
            }
        }
    }
}

private sealed interface GridCell {
    data class Image(val cardImage: CardImageDefault) : GridCell
    data object Camera : GridCell
    data object Placeholder : GridCell
}


@Preview(showBackground = true)
@Composable
private fun FilteredImageGridPreview() {
    val filters = listOf("컬러", "자연", "감성", "푸드", "추상", "메모")

    val dummyImages = mapOf(
        "컬러" to listOf(
            R.drawable.bg_color_blue, R.drawable.bg_color_green, R.drawable.bg_color_yellow,
            R.drawable.bg_color_orange, R.drawable.bg_color_red, R.drawable.bg_color_purple, R.drawable.bg_color_pink
        ),
        "자연" to listOf(
            R.drawable.bg_netural_leaf, R.drawable.bg_netural_sea, R.drawable.bg_netural_sand,
            R.drawable.bg_netural_cloud, R.drawable.bg_netural_snow, R.drawable.bg_netural_flower, R.drawable.bg_netural_moon
        ),

        "감성" to listOf(
            R.drawable.bg_emotion_bed, R.drawable.bg_emotion_shadow, R.drawable.bg_emotion_airplane,
            R.drawable.bg_emotion_cat, R.drawable.bg_emotion_window, R.drawable.bg_emotion_light, R.drawable.bg_emotion_book
        ),

        "푸드" to listOf(
            R.drawable.bg_food_coffee, R.drawable.bg_food_icecream, R.drawable.bg_food_cake,
            R.drawable.bg_food_lemon, R.drawable.bg_food_candy, R.drawable.bg_food_cupcake, R.drawable.bg_food_beer
        ),

        "추상" to listOf(
            R.drawable.bg_abstract_1, R.drawable.bg_abstract_2, R.drawable.bg_abstract_3,
            R.drawable.bg_abstract_4, R.drawable.bg_abstract_5, R.drawable.bg_abstract_6, R.drawable.bg_abstract_7
        ),

        "메모" to listOf(
            R.drawable.bg_memo_1, R.drawable.bg_memo_2, R.drawable.bg_memo_3,
            R.drawable.bg_memo_4, R.drawable.bg_memo_5, R.drawable.bg_memo_6, R.drawable.bg_memo_7
        )
    )
    var selectedFilter by remember { mutableStateOf(filters.first()) }
    var selectedImage by remember { mutableStateOf<Int?>(null) }

    val currentImages by remember(selectedFilter) {
        derivedStateOf { dummyImages[selectedFilter].orEmpty() }
    }

    LaunchedEffect(currentImages) {
        if (currentImages.isNotEmpty()) {
            selectedImage = currentImages.first()
        }
    }


    FilteredImageGrid(
        filters = filters,
        selectedFilter = selectedFilter,
        selectedImageName = null,
        onFilterSelected = { selectedFilter = it },
        onImageSelected = { resId ->
            println("이미지 선택됨: $resId")
        },
        cardDefaultImagesByCategory = emptyMap(),
        onCameraClick = {
            println("카메라 클릭")
        }
    )
}


private const val TAG = "GridComponent"
