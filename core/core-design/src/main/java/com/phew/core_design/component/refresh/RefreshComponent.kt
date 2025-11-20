package com.phew.core_design.component.refresh

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phew.core_design.R

val TOP_CONTENT_OFFSET = 72.dp

@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.pullToRefreshOffset(
    state: PullToRefreshState,
    baseOffset: Dp,
    pullThreshold: Dp = 100.dp
): Modifier = this.graphicsLayer {
    val pullDistancePx = state.distanceFraction * pullThreshold.toPx()
    translationY = pullDistancePx + baseOffset.toPx()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshBox(
    isRefresh : Boolean,
    onRefresh : () -> Unit,
    state : PullToRefreshState,
    paddingValues: PaddingValues,
    content : @Composable (() -> Unit)
){
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.ic_refresh)
    )
    val refreshProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        restartOnPlay = isRefresh
    )
    PullToRefreshBox(
        isRefreshing = isRefresh,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxWidth(),
        state = state,
        indicator = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(top = paddingValues.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) {
                val progress = if (isRefresh) refreshProgress else state.distanceFraction
                if (isRefresh || state.distanceFraction > 0f) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }
    ) {
        content()
    }
}