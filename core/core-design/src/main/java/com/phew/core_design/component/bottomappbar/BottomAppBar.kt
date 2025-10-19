package com.phew.core_design.component.bottomappbar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent

@Composable
fun SooumNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope. () -> Unit
) {
    NavigationBarLayout(
        modifier = modifier,
        contentColor = NavigationDefaults.navigationContentColor(),
        containerColor = NavigationDefaults.navigationContainerColor(),
        tonalElevation = 0.dp,
        content = content,
    )
}

@Composable
fun RowScope.SooumNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    selectedIcon: @Composable () -> Unit = icon,
    enabled: Boolean = true,
) {
    NavigationBarItemLayout(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        colors = NavigationDefaults.colors(),
    )
}

@Composable
private fun NavigationBarLayout(
    modifier: Modifier = Modifier,
    containerColor: Color = NavigationDefaults.navigationContainerColor(),
    contentColor: Color = NavigationDefaults.navigationContentColor(),
    tonalElevation: Dp = NavigationBarDefaults.Elevation,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit
) {
    val cornerShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

    Surface(
        modifier = modifier,
        color = containerColor,
        shape = cornerShape,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        border = BorderStroke(1.dp, NeutralColor.GRAY_200)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(windowInsets)
                .height(NavigationDefaults.barHeight)
                .padding(start = 16.dp, bottom = 8.dp, top = 8.dp, end = 16.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            content = content
        )
    }
}

@Composable
private fun RowScope.NavigationBarItemLayout(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: NavigationBarItemColors = NavigationDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val haptic = LocalHapticFeedback.current
    val isPressed by interactionSource.collectIsPressedAsState()
    val sizeScale by with(NavigationDefaults) {
        animateFloatAsState(
            if (isPressed) SCALE_TO else SCALE_FROM, animationSpec = spring(
                stiffness = STIFFNESS
            )
        )
    }

    val styledLabel: @Composable () -> Unit = label.let {
        @Composable {
            val style = TextComponent.CAPTION_1_SB_12
            val textColor by colors.textColor(selected = selected)

            CompositionLocalProvider(LocalContentColor provides textColor) {
                ProvideTextStyle(style, content = label)
            }
        }
    }

    Box(
        modifier
            .selectable(
                selected = selected,
                onClick = {
                    onClick()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                enabled = enabled,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
            )
            .graphicsLayer {
                scaleX = sizeScale
                scaleY = sizeScale
            }
            .weight(1f),
        contentAlignment = Alignment.TopCenter
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            icon()
            Spacer(
                Modifier
                    .height(2.dp)
                    .fillMaxWidth()
            )
            styledLabel()
        }
    }
}

object NavigationDefaults {
    internal const val SCALE_TO = 0.85f
    internal const val SCALE_FROM = 1f
    internal const val STIFFNESS = 6000F
    internal val horizontalPadding = 16.dp
    internal val verticalPadding = 8.dp

    val barHeight = 62.dp

    val navigationBarPadding = (barHeight + verticalPadding) * 0.7f

    @Composable
    fun navigationContainerColor() = NeutralColor.WHITE

    @Composable
    fun navigationContentColor() = NeutralColor.GRAY_300

    @Composable
    fun navigationSelectedTextColor() = NeutralColor.BLACK

    @Composable
    fun colors() = NavigationDefaults.navigationItemColors(
        selectedTextColor = navigationSelectedTextColor(),
        unselectedTextColor = NeutralColor.GRAY_300
    )
}

@Composable
internal fun NavigationDefaults.navigationItemColors(
    selectedIconColor: Color = Color.Transparent,
    selectedTextColor: Color,
    unselectedIconColor: Color = Color.Transparent,
    unselectedTextColor: Color,
): NavigationBarItemColors = DefaultNavigationBarItemColors(
    selectedIconColor = selectedIconColor,
    selectedTextColor = selectedTextColor,
    unselectedIconColor = unselectedIconColor,
    unselectedTextColor = unselectedTextColor
)

@Stable
interface NavigationBarItemColors {
    /**
     * Represents the icon color for this item, depending on whether it is [selected].
     * @param selected whether the item is selected
     */
    @Composable
    fun iconColor(selected: Boolean): State<Color>

    /**
     * Represents the text color for this item, depending on whether it is [selected].
     * @param selected whether the item is selected
     */
    @Composable
    fun textColor(selected: Boolean): State<Color>
}


@Stable
private class DefaultNavigationBarItemColors(
    private val selectedIconColor: Color,
    private val selectedTextColor: Color,
    private val unselectedIconColor: Color,
    private val unselectedTextColor: Color,
) : NavigationBarItemColors {
    @Composable
    override fun iconColor(selected: Boolean): State<Color> {
        return animateColorAsState(
            targetValue = if (selected) selectedIconColor else unselectedIconColor,
            animationSpec = tween(ItemAnimationDurationMillis)
        )
    }

    /**
     * Represents the text color for this item, depending on whether it is [selected].
     * @param selected whether the item is selected
     */
    @Composable
    override fun textColor(selected: Boolean): State<Color> {
        return animateColorAsState(
            targetValue = if (selected) selectedTextColor else unselectedTextColor,
            animationSpec = tween(ItemAnimationDurationMillis)
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is DefaultNavigationBarItemColors) return false

        if (selectedIconColor != other.selectedIconColor) return false
        if (unselectedIconColor != other.unselectedIconColor) return false
        if (selectedTextColor != other.selectedTextColor) return false
        if (unselectedTextColor != other.unselectedTextColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = selectedIconColor.hashCode()
        result = 31 * result + unselectedIconColor.hashCode()
        result = 31 * result + selectedTextColor.hashCode()
        result = 31 * result + unselectedTextColor.hashCode()
        return result
    }

    companion object {
        private const val ItemAnimationDurationMillis: Int = 100
    }
}
