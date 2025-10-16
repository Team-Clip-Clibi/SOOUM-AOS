package com.phew.core_design

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


object BottomSheetComponent {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BottomSheet(
        data: ArrayList<BottomSheetItem>,
        onItemClick: (Int) -> Unit,
        onDismiss: () -> Unit
    ) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = false
        )
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            dragHandle = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_handle),
                        contentDescription = null,
                        tint = NeutralColor.GRAY_300,
                        modifier = Modifier
                            .width(44.dp)
                            .height(8.dp)
                    )
                }
            },
            containerColor = NeutralColor.WHITE
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
            ) {
                items(data) { viewItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable { onItemClick(viewItem.id) }
                            .padding(start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = viewItem.title,
                            style = TextComponent.SUBTITLE_1_M_16,
                            color = NeutralColor.GRAY_500,
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
    val title: String
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
