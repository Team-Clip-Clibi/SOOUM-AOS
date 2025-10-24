package com.phew.core_design.component.card.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor

@Composable
fun IndicatorDot(
    modifier: Modifier,
    pagerState: PagerState,
    totalSize: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
    ) {
        repeat(totalSize) { iteration ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(80.dp))
                    .background(NeutralColor.GRAY_600.copy(alpha = if (iteration == pagerState.currentPage % totalSize) 0.8f else 0.2f))
                    .width(if (iteration == pagerState.currentPage % totalSize) 8.dp else 4.dp)
                    .height(4.dp)
            )
        }
    }
}