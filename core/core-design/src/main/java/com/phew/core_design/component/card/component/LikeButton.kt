package com.phew.core_design.component.card.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.phew.core_design.Danger
import com.phew.core_design.NeutralColor
import com.phew.core_design.R
import com.phew.core_design.TextComponent

private val Snappy = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private val Overshoot = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

@Composable
fun LikeButton(
    modifier: Modifier = Modifier,
    likeCount: String,
    isLike: Boolean,
    isLikeLoading: Boolean = false,
    likeAnimationKey: Int = 0,
    textStyle: TextStyle = TextComponent.BODY_1_M_14,
    iconSize: Dp = 20.dp,
    spacing: Dp = 2.dp,
    likedColor: Color = Danger.M_RED,
    unlikedColor: Color = NeutralColor.GRAY_500,
    onClickLike: () -> Unit,
) {
    val bounce = remember { Animatable(1f) }

    LaunchedEffect(likeAnimationKey) {
        if (likeAnimationKey == 0) return@LaunchedEffect

        bounce.snapTo(1f)
        bounce.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = 420
                1f at 0
                1.10f at 147 using Snappy
                0.95f at 273
                1f at 420
            }
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = modifier.clickable(
            enabled = !isLikeLoading,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClickLike,
        )
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer {
                    scaleX = bounce.value
                    scaleY = bounce.value
                }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_heart_stoke),
                contentDescription = null,
                tint = unlikedColor,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = if (isLike) 0f else 1f }
            )
            Icon(
                painter = painterResource(R.drawable.ic_heart_filled),
                contentDescription = "좋아요",
                tint = likedColor,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = if (isLike) 1f else 0f
                    }
            )
        }
        LikeCount(
            count = likeCount,
            isLike = isLike,
            textStyle = textStyle,
            likedColor = likedColor,
            unlikedColor = unlikedColor,
        )
    }
}

@Composable
private fun LikeCount(
    count: String,
    isLike: Boolean,
    textStyle: TextStyle,
    likedColor: Color,
    unlikedColor: Color,
) {
    val color by animateColorAsState(
        targetValue = if (isLike) likedColor else unlikedColor,
        label = "LikeCountColor",
    )

    Row {
        count.forEachIndexed { index, char ->
            key(index) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        val direction = if (isLike) 1 else -1
                        slideInVertically(
                            animationSpec = tween(360, easing = Overshoot)
                        ) { height -> height * direction }
                            .togetherWith(
                                slideOutVertically(
                                    animationSpec = tween(360, easing = Overshoot)
                                ) { height -> -height * direction }
                            )
                            .using(SizeTransform(clip = true))
                    },
                    label = "LikeDigit",
                ) { digit ->
                    Text(
                        text = digit.toString(),
                        style = textStyle,
                        color = color,
                    )
                }
            }
        }
    }
}
