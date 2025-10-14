package com.phew.core_design.component.card.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor

@Composable
internal fun IndicatorDot(
    pagerState: PagerState,
    totalSize: Int
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(totalSize) { interation ->
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(RoundedCornerShape(80.dp))
                    .background(NeutralColor.GRAY_600.copy(alpha = if (pagerState.currentPage == interation) 0.8f else 0.2f))
                    .size(4.dp)
            )
        }
    }
}