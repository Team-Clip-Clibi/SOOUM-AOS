package com.phew.core_design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Tab
import androidx.compose.material3.Text

object TabBar {
    @Composable
    fun TabBarTwo(
        data: List<String>,
        selectTabData: Int,
        onFirstItemClick: () -> Unit,
        onSecondItemClick: () -> Unit,
    ) {
        TabRow(
            selectedTabIndex = selectTabData,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(color = NeutralColor.WHITE),
            contentColor = NeutralColor.WHITE,
            containerColor = NeutralColor.WHITE,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectTabData]),
                    height = 2.dp,
                    color = NeutralColor.BLACK
                )
            },
            divider = {}
        ) {
            data.forEachIndexed { index, title ->
                val isSelected = selectTabData == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        when (index) {
                            0 -> onFirstItemClick()
                            1 -> onSecondItemClick()
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            style = TextComponent.TITLE_2_SB_16,
                            color = if (isSelected) NeutralColor.BLACK else NeutralColor.GRAY_400,
                        )
                    }
                )
            }
        }
    }
}