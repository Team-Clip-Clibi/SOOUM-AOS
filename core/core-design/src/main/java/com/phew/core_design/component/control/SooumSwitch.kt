package com.phew.core_design.component.control

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SooumSwitch(
    modifier: Modifier = Modifier,
    label: String? = null,
    isSelected: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    SooumSwitchImpl(
        modifier = modifier,
        checked = isSelected,
        onCheckedChange = {
            onClick.invoke()
        },
        enabled = !isDisabled,
        labelContent = if (!label.isNullOrEmpty()) {
            {
                Text(
                    modifier = Modifier.weight(1f, fill = true),
                    text = label,
                    style = TextComponent.CAPTION_2_M_12
                )
            }
        } else null,
        interactionSource = interactionSource
    )
}

@Composable
@Suppress("ComposableLambdaParameterNaming", "ComposableLambdaParameterPosition")
internal fun SooumSwitchImpl(
    modifier: Modifier,
    checked: Boolean,
    onCheckedChange: (() -> Unit)?,
    thumbContent: (@Composable () -> Unit)? = null,
    labelContent: (@Composable RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val thumbPaddingStart = 4.dp
    val minBound = with(LocalDensity.current) { thumbPaddingStart.toPx() }
    val maxBound = with(LocalDensity.current) { ThumbPathLength.toPx() }
    val valueToOffset = remember<(Boolean) -> Float>(minBound, maxBound) {
        { value -> if (value) maxBound else minBound }
    }

    val targetValue = valueToOffset(checked)
    val offset = remember { Animatable(targetValue) }
    val scope = rememberCoroutineScope()

    SideEffect {
        // min bound might have changed if the icon is only rendered in checked state.
        offset.updateBounds(lowerBound = minBound)
    }

    DisposableEffect(checked) {
        if (offset.targetValue != targetValue) {
            scope.launch {
                offset.animateTo(targetValue, AnimationSpec)
            }
        }
        onDispose { }
    }

    val toggleableModifier =
        if (onCheckedChange != null) {
            val toggleableState = ToggleableState(checked)
            Modifier.triStateToggleable(
                state = toggleableState,
                onClick = onCheckedChange,
                enabled = enabled,
                role = Role.Switch,
                indication = null,
                interactionSource = interactionSource
            )
        } else {
            Modifier
        }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SwitchTokens.SwitchHorizontalSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        labelContent?.let { content ->
            CompositionLocalProvider(
                value = LocalContentColor provides SwitchTokens.labelColor,
                content = {
                    content.invoke(this)
                }
            )
        }

        Box(
            Modifier
                .then(toggleableModifier)
                .wrapContentSize(Alignment.Center)
                .requiredSize(SwitchWidth, SwitchHeight)
        ) {
            SwitchImpl(
                checked = checked,
                enabled = enabled,
                colors = colors,
                thumbValue = offset.asState(),
                thumbShape = SwitchTokens.HandleShape,
                thumbContent = thumbContent,
            )
        }
    }
}

@Composable
@Suppress("ComposableLambdaParameterNaming", "ComposableLambdaParameterPosition")
private fun BoxScope.SwitchImpl(
    checked: Boolean,
    enabled: Boolean,
    colors: SwitchColors,
    thumbValue: State<Float>,
    thumbContent: (@Composable () -> Unit)?,
    thumbShape: Shape,
) {
    val trackColor by colors.trackColor(enabled, checked)
    val thumbSizeDp = 24.dp

    val thumbOffset = thumbValue.value

    val trackShape = SwitchTokens.TrackShape
    val modifier = Modifier
        .align(Alignment.Center)
        .width(SwitchWidth)
        .height(SwitchHeight)
        .border(
            SwitchTokens.TrackOutlineWidth,
            colors.borderColor(enabled, checked).value,
            trackShape
        )
        .background(trackColor, trackShape)

    Box(modifier) {
        val thumbColor by colors.thumbColor(enabled, checked)
        val resolvedThumbColor = thumbColor
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset(thumbOffset.roundToInt(), 0) }
                .requiredSize(thumbSizeDp)
                .background(resolvedThumbColor, thumbShape),
            contentAlignment = Alignment.Center
        ) {
            if (thumbContent != null) {
                val iconColor = colors.iconColor(enabled, checked)
                CompositionLocalProvider(
                    LocalContentColor provides iconColor.value,
                    content = thumbContent
                )
            }
        }
    }
}

internal val ThumbDiameter = SwitchTokens.SelectedHandleWidth
internal val UncheckedThumbDiameter = SwitchTokens.UnselectedHandleWidth
private val SwitchWidth = SwitchTokens.TrackWidth
private val SwitchHeight = SwitchTokens.TrackHeight
private val ThumbPadding = (SwitchHeight - ThumbDiameter) / 2
private val ThumbPathLength = (SwitchWidth - ThumbDiameter) - ThumbPadding

private val AnimationSpec = TweenSpec<Float>(durationMillis = 200)

/**
 * Contains the default values used by [Switch]
 */
internal object SwitchDefaults {
    /**
     * Creates a [SwitchColors] that represents the different colors used in a [Switch] in
     * different states.
     *
     * @param checkedThumbColor the color used for the thumb when enabled and checked
     * @param checkedTrackColor the color used for the track when enabled and checked
     * @param checkedBorderColor the color used for the border when enabled and checked
     * @param checkedIconColor the color used for the icon when enabled and checked
     * @param uncheckedThumbColor the color used for the thumb when enabled and unchecked
     * @param uncheckedTrackColor the color used for the track when enabled and unchecked
     * @param uncheckedBorderColor the color used for the border when enabled and unchecked
     * @param uncheckedIconColor the color used for the icon when enabled and unchecked
     * @param disabledCheckedThumbColor the color used for the thumb when disabled and checked
     * @param disabledCheckedTrackColor the color used for the track when disabled and checked
     * @param disabledCheckedBorderColor the color used for the border when disabled and checked
     * @param disabledCheckedIconColor the color used for the icon when disabled and checked
     * @param disabledUncheckedThumbColor the color used for the thumb when disabled and unchecked
     * @param disabledUncheckedTrackColor the color used for the track when disabled and unchecked
     * @param disabledUncheckedBorderColor the color used for the border when disabled and unchecked
     * @param disabledUncheckedIconColor the color used for the icon when disabled and unchecked
     */
    @Composable
    fun colors(
        checkedThumbColor: Color = SwitchTokens.SelectedHandleColor,
        checkedTrackColor: Color = SwitchTokens.SelectedTrackColor,
        checkedBorderColor: Color = Color.Transparent,
        checkedIconColor: Color = SwitchTokens.SelectedIconColor,
        uncheckedThumbColor: Color = SwitchTokens.UnselectedHandleColor,
        uncheckedTrackColor: Color = SwitchTokens.UnselectedTrackColor,
        uncheckedBorderColor: Color = SwitchTokens.UnselectedFocusTrackOutlineColor,
        uncheckedIconColor: Color = SwitchTokens.UnselectedIconColor,
        disabledCheckedThumbColor: Color = SwitchTokens.DisabledSelectedHandleColor
            .copy(alpha = SwitchTokens.DisabledSelectedHandleOpacity)
            .compositeOver(MaterialTheme.colorScheme.surface),
        disabledCheckedTrackColor: Color = SwitchTokens.DisabledSelectedTrackColor
            .copy(alpha = SwitchTokens.DisabledTrackOpacity)
            .compositeOver(MaterialTheme.colorScheme.surface),
        disabledCheckedBorderColor: Color = Color.Transparent,
        disabledCheckedIconColor: Color = SwitchTokens.DisabledSelectedIconColor
            .copy(alpha = SwitchTokens.DisabledSelectedIconOpacity)
            .compositeOver(MaterialTheme.colorScheme.surface),
        disabledUncheckedThumbColor: Color = SwitchTokens.DisabledUnselectedHandleColor
            .copy(alpha = SwitchTokens.DisabledUnselectedHandleOpacity)
            .compositeOver(MaterialTheme.colorScheme.surface),
        disabledUncheckedTrackColor: Color = SwitchTokens.DisabledUnselectedTrackColor
            .copy(alpha = SwitchTokens.DisabledTrackOpacity)
            .compositeOver(MaterialTheme.colorScheme.surface),
        disabledUncheckedBorderColor: Color =
            SwitchTokens.DisabledUnselectedTrackOutlineColor
                .copy(alpha = SwitchTokens.DisabledTrackOpacity)
                .compositeOver(MaterialTheme.colorScheme.surface),
        disabledUncheckedIconColor: Color = SwitchTokens.DisabledUnselectedIconColor
            .copy(alpha = SwitchTokens.DisabledUnselectedIconOpacity)
            .compositeOver(MaterialTheme.colorScheme.surface),
        labelColor: Color = SwitchTokens.labelColor
    ): SwitchColors = SwitchColors(
        checkedThumbColor = checkedThumbColor,
        checkedTrackColor = checkedTrackColor,
        checkedBorderColor = checkedBorderColor,
        checkedIconColor = checkedIconColor,
        uncheckedThumbColor = uncheckedThumbColor,
        uncheckedTrackColor = uncheckedTrackColor,
        uncheckedBorderColor = uncheckedBorderColor,
        uncheckedIconColor = uncheckedIconColor,
        disabledCheckedThumbColor = disabledCheckedThumbColor,
        disabledCheckedTrackColor = disabledCheckedTrackColor,
        disabledCheckedBorderColor = disabledCheckedBorderColor,
        disabledCheckedIconColor = disabledCheckedIconColor,
        disabledUncheckedThumbColor = disabledUncheckedThumbColor,
        disabledUncheckedTrackColor = disabledUncheckedTrackColor,
        disabledUncheckedBorderColor = disabledUncheckedBorderColor,
        disabledUncheckedIconColor = disabledUncheckedIconColor,
        labelColor = labelColor
    )

    /**
     * Icon size to use for `thumbContent`
     */
    val IconSize = 16.dp
}

/**
 * Represents the colors used by a [Switch] in different states
 *
 * See [SwitchDefaults.colors] for the default implementation that follows Material
 * specifications.
 */
@Immutable
class SwitchColors internal constructor(
    private val checkedThumbColor: Color,
    private val checkedTrackColor: Color,
    private val checkedBorderColor: Color,
    private val checkedIconColor: Color,
    private val uncheckedThumbColor: Color,
    private val uncheckedTrackColor: Color,
    private val uncheckedBorderColor: Color,
    private val uncheckedIconColor: Color,
    private val disabledCheckedThumbColor: Color,
    private val disabledCheckedTrackColor: Color,
    private val disabledCheckedBorderColor: Color,
    private val disabledCheckedIconColor: Color,
    private val disabledUncheckedThumbColor: Color,
    private val disabledUncheckedTrackColor: Color,
    private val disabledUncheckedBorderColor: Color,
    private val disabledUncheckedIconColor: Color,
    private val labelColor: Color
) {
    /**
     * Represents the color used for the switch's thumb, depending on [enabled] and [checked].
     *
     * @param enabled whether the [Switch] is enabled or not
     * @param checked whether the [Switch] is checked or not
     */
    @Composable
    internal fun thumbColor(enabled: Boolean, checked: Boolean): State<Color> {
        return rememberUpdatedState(
            if (enabled) {
                if (checked) checkedThumbColor else uncheckedThumbColor
            } else {
                if (checked) disabledCheckedThumbColor else disabledUncheckedThumbColor
            }
        )
    }

    /**
     * Represents the color used for the switch's track, depending on [enabled] and [checked].
     *
     * @param enabled whether the [Switch] is enabled or not
     * @param checked whether the [Switch] is checked or not
     */
    @Composable
    internal fun trackColor(enabled: Boolean, checked: Boolean): State<Color> {
        val targetColor = if (enabled) {
            if (checked) checkedTrackColor else uncheckedTrackColor
        } else {
            if (checked) disabledCheckedTrackColor else disabledUncheckedTrackColor
        }
        
        return animateColorAsState(
            targetValue = targetColor,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 200,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            ),
            label = "SwitchTrackColor"
        )
    }

    /**
     * Represents the color used for the switch's border, depending on [enabled] and [checked].
     *
     * @param enabled whether the [Switch] is enabled or not
     * @param checked whether the [Switch] is checked or not
     */
    @Composable
    internal fun borderColor(enabled: Boolean, checked: Boolean): State<Color> {
        return rememberUpdatedState(
            if (enabled) {
                if (checked) checkedBorderColor else uncheckedBorderColor
            } else {
                if (checked) disabledCheckedBorderColor else disabledUncheckedBorderColor
            }
        )
    }

    /**
     * Represents the content color passed to the icon if used
     *
     * @param enabled whether the [Switch] is enabled or not
     * @param checked whether the [Switch] is checked or not
     */
    @Composable
    internal fun iconColor(enabled: Boolean, checked: Boolean): State<Color> {
        return rememberUpdatedState(
            if (enabled) {
                if (checked) checkedIconColor else uncheckedIconColor
            } else {
                if (checked) disabledCheckedIconColor else disabledUncheckedIconColor
            }
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is SwitchColors) return false

        if (checkedThumbColor != other.checkedThumbColor) return false
        if (checkedTrackColor != other.checkedTrackColor) return false
        if (checkedBorderColor != other.checkedBorderColor) return false
        if (checkedIconColor != other.checkedIconColor) return false
        if (uncheckedThumbColor != other.uncheckedThumbColor) return false
        if (uncheckedTrackColor != other.uncheckedTrackColor) return false
        if (uncheckedBorderColor != other.uncheckedBorderColor) return false
        if (uncheckedIconColor != other.uncheckedIconColor) return false
        if (disabledCheckedThumbColor != other.disabledCheckedThumbColor) return false
        if (disabledCheckedTrackColor != other.disabledCheckedTrackColor) return false
        if (disabledCheckedBorderColor != other.disabledCheckedBorderColor) return false
        if (disabledCheckedIconColor != other.disabledCheckedIconColor) return false
        if (disabledUncheckedThumbColor != other.disabledUncheckedThumbColor) return false
        if (disabledUncheckedTrackColor != other.disabledUncheckedTrackColor) return false
        if (disabledUncheckedBorderColor != other.disabledUncheckedBorderColor) return false
        if (disabledUncheckedIconColor != other.disabledUncheckedIconColor) return false
        if (labelColor != other.labelColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = checkedThumbColor.hashCode()
        result = 31 * result + checkedTrackColor.hashCode()
        result = 31 * result + checkedBorderColor.hashCode()
        result = 31 * result + checkedIconColor.hashCode()
        result = 31 * result + uncheckedThumbColor.hashCode()
        result = 31 * result + uncheckedTrackColor.hashCode()
        result = 31 * result + uncheckedBorderColor.hashCode()
        result = 31 * result + uncheckedIconColor.hashCode()
        result = 31 * result + disabledCheckedThumbColor.hashCode()
        result = 31 * result + disabledCheckedTrackColor.hashCode()
        result = 31 * result + disabledCheckedBorderColor.hashCode()
        result = 31 * result + disabledCheckedIconColor.hashCode()
        result = 31 * result + disabledUncheckedThumbColor.hashCode()
        result = 31 * result + disabledUncheckedTrackColor.hashCode()
        result = 31 * result + disabledUncheckedBorderColor.hashCode()
        result = 31 * result + disabledUncheckedIconColor.hashCode()
        result = 31 * result + labelColor.hashCode()
        return result
    }
}

internal object SwitchTokens {
    val DisabledSelectedHandleColor = NeutralColor.WHITE // (비 활성화, 선택됨) 일 때 원 색상
    const val DisabledSelectedHandleOpacity = 1.0f
    val DisabledSelectedIconColor = Color.Black // 현재 쓰이는 곳 없음.
    const val DisabledSelectedIconOpacity = 1.0f
    val DisabledSelectedTrackColor = NeutralColor.GRAY_200 // (비 활성화, 선택됨) 일 때 배경 색상
    const val DisabledTrackOpacity = 1.0f
    val DisabledUnselectedHandleColor = NeutralColor.WHITE // (비 활성화, 선택안 됨) 일 때 원 색상
    const val DisabledUnselectedHandleOpacity = 1.0f
    val DisabledUnselectedIconColor = Color.Black // 현재 쓰이는 곳 없음.
    const val DisabledUnselectedIconOpacity = 1.0f
    val DisabledUnselectedTrackColor = NeutralColor.GRAY_200 // (비 활성화, 선택 안됨) 일 때 배경 색상
    val DisabledUnselectedTrackOutlineColor =  NeutralColor.GRAY_200 // (비 활성화, 선택됨) 일 때 배경 아웃 라인 색상
    val HandleShape = CircleShape // 원 Shape
    val PressedHandleHeight = 24.0.dp
    val PressedHandleWidth = 24.0.dp
    val SelectedFocusHandleColor = Color.Black // 안 쓰는중.
    val SelectedFocusIconColor = Color.Black // 안 쓰는중.
    val SelectedFocusTrackColor = Color.Black // 안 쓰는중.
    val SelectedHandleColor = NeutralColor.WHITE
    val SelectedHandleHeight = 24.0.dp
    val SelectedHandleWidth = 24.0.dp
    val SelectedHoverHandleColor = Color.Black // 안 쓰는중.
    val SelectedHoverIconColor = Color.Black // 안 쓰는중.
    val SelectedHoverTrackColor = Color.Black // 안 쓰는중.
    val SelectedIconColor = Color.Black // 안 쓰는중.
    val SelectedIconSize = 16.0.dp
    val SelectedPressedHandleColor = Color.Black // 안 쓰는중.
    val SelectedPressedIconColor = Color.Black // 안 쓰는중.
    val SelectedPressedTrackColor = Color.Black // 안 쓰는중.
    val SelectedTrackColor = Primary.MAIN // (활성화, 선택됨) 일 때 배경 색상
    val StateLayerSize = 52.0.dp
    val TrackHeight = 32.0.dp
    val TrackOutlineWidth = 1.0.dp
    val TrackShape = RoundedCornerShape(100.dp) // 배경 Shape
    val TrackWidth = 56.0.dp
    val UnselectedFocusHandleColor = Color.Black // 안 쓰는중.
    val UnselectedFocusIconColor = Color.Black // 안 쓰는중.
    val UnselectedFocusTrackColor = Color.Black // 안 쓰는중.
    val UnselectedFocusTrackOutlineColor = NeutralColor.GRAY_200  // (활성화, 선택 안됨) 일 때 배경 아웃 라인 색상
    val UnselectedHandleColor = NeutralColor.WHITE // (활성화, 선택 안됨) 일 때 원 색상
    val UnselectedHandleHeight = 24.0.dp
    val UnselectedHandleWidth = 24.0.dp
    val UnselectedHoverHandleColor = Color.Black // 안 쓰는중.
    val UnselectedHoverIconColor = Color.Black // 안 쓰는중.
    val UnselectedHoverTrackColor = Color.Black // 안 쓰는중.
    val UnselectedHoverTrackOutlineColor = Color.Black // 안 쓰는중.
    val UnselectedIconColor = Color.Black // 안 쓰는중.
    val UnselectedIconSize = 16.0.dp
    val UnselectedPressedHandleColor = Color.Black // 안 쓰는중.
    val UnselectedPressedIconColor = Color.Black // 안 쓰는중.
    val UnselectedPressedTrackColor = Color.Black // 안 쓰는중.
    val UnselectedPressedTrackOutlineColor = Color.Black // 안 쓰는중.
    val UnselectedTrackColor = NeutralColor.GRAY_200  // (활성화, 선택 안됨) 일 때 배경 색상
    val UnselectedTrackOutlineColor = Color.Black // 안 쓰는중.
    val IconHandleHeight = 32.0.dp
    val IconHandleWidth = 24.0.dp

    // DIMENSION
    val SwitchHorizontalSpacing = 4.dp

    // COLOR
    val labelColor = NeutralColor.BLACK
}
