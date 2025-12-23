package com.phew.core_design.component.refresh

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phew.core_design.NeutralColor
import com.phew.core_design.R

@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.pullToRefreshOffset(
    state: PullToRefreshState,
    baseOffset: Dp,
    pullThreshold: Dp = 100.dp,
): Modifier = this.graphicsLayer {
    val pullDistancePx = state.distanceFraction * pullThreshold.toPx()
    translationY = pullDistancePx + baseOffset.toPx()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshBox(
    modifier: Modifier = Modifier,
    isRefresh: Boolean,
    backgroundColor: Color = NeutralColor.WHITE,
    onRefresh: () -> Unit,
    state: PullToRefreshState,
    paddingValues: PaddingValues,
    indicatorTopPadding: Dp = 0.dp,
    content: @Composable (() -> Unit),
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.ic_refresh)
    )
    val refreshProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isRefresh,
        restartOnPlay = true
    )
    PullToRefreshBox(
        isRefreshing = isRefresh,
        onRefresh = onRefresh,
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor),
        state = state,
        indicator = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = paddingValues.calculateTopPadding() + indicatorTopPadding)
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                val currentProgress = if (isRefresh) refreshProgress else 0f
                if (isRefresh || state.distanceFraction > 0f) {
                    LottieAnimation(
                        composition = composition,
                        progress = { currentProgress },
                        modifier = Modifier
                            .size(44.dp)
                            .graphicsLayer {
                                alpha =
                                    if (isRefresh) 1f else state.distanceFraction.coerceIn(0f, 1f)
                                rotationZ =  0f
                            }
                    )
                }
            }
        }
    ) {
        content()
    }
}