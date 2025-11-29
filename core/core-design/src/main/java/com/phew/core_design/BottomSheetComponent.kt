package com.phew.core_design

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.drawBehind

object BottomSheetComponent {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BottomSheet(
        data: ArrayList<BottomSheetItem>,
        onItemClick: (Int) -> Unit,
        onDismiss: () -> Unit,
    ) {

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        ModalBottomSheet(
            modifier = Modifier.padding(start= 16.dp , end = 16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(vertical = 10.dp),
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = NeutralColor.WHITE,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(data) { viewItem ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .drawBehind {
                                val color = if(isPressed) {
                                    NeutralColor.GRAY_100
                                } else {
                                    NeutralColor.WHITE
                                }
                                drawRect(color)
                            }
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                enabled = true,
                                onClick = {
                                    onItemClick(viewItem.id)
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically, // 아이콘/텍스트 세로 정렬 유지
                    ) {
                        if (viewItem.image != -1) {
                            Image(
                                painter = painterResource(viewItem.image),
                                contentDescription = viewItem.title,
                                colorFilter = ColorFilter.tint(viewItem.imageColor)
                            )
                        }
                        Text(
                            text = viewItem.title,
                            style = TextComponent.SUBTITLE_1_M_16,
                            color = viewItem.textColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}


data class BottomSheetItem(
    val id: Int,
    val title: String,
    val image: Int = -1, // 이미지가 필요할 경우를 대비해 추가,
    val textColor: Color = NeutralColor.GRAY_500,
    val imageColor: Color = NeutralColor.GRAY_500,
)

@Composable
@Preview
private fun Preview() {
    val testData = arrayListOf(
        BottomSheetItem(id = 0, title = "Label"),
        BottomSheetItem(id = 1, title = "Label"),
        BottomSheetItem(id = 2, title = "Label"),
        BottomSheetItem(id = 3, title = "Label"),
        BottomSheetItem(id = 4, title = "Label"),
    )
    BottomSheetComponent.BottomSheet(
        data = testData,
        onItemClick = {

        },
        onDismiss = {

        }
    )
}