package com.phew.core_design.component.topappbar

import androidx.annotation.FloatRange
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import kotlin.math.abs

/**
 *  TODO 추후 보완
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SooumMainTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    navigation: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = SooumTopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    titleHorizontalAlignment: Alignment.Horizontal =
        if (navigation != null) Alignment.Start else Alignment.CenterHorizontally,
) {
    if(titleHorizontalAlignment  == Alignment.CenterHorizontally) {
        CenterAlignedTopAppBar(
            modifier = modifier,
            title = title,
            navigationIcon = navigation ?: {},
            actions = actions ?: {},
            windowInsets = windowInsets,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = colors.containerColor(),
                navigationIconContentColor = colors.navigationIconContentColor(),
                actionIconContentColor = colors.actionIconContentColor(),
            ),
            scrollBehavior = scrollBehavior
        )
    } else {
        TopAppBar(
            modifier = modifier,
            title = title,
            navigationIcon = navigation ?: {},
            actions = actions ?: {},
            windowInsets = windowInsets,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.containerColor(),
                navigationIconContentColor = colors.navigationIconContentColor(),
                actionIconContentColor = colors.actionIconContentColor(),
            ),
            scrollBehavior = scrollBehavior
        )
    }
}

/**
 * The base [Layout] for all top app bars. This function lays out a top app bar navigation icon
 * (leading icon), a title (header), and action icons (trailing icons). Note that the navigation and
 * the actions are optional.
 *
 * @param heightPx the total height this layout is capped to
 * @param navigationIconContentColor the content color that will be applied via a
 * [LocalContentColor] when composing the navigation icon
 * @param actionIconContentColor the content color that will be applied via a [LocalContentColor]
 * when composing the action icons
 * @param modifier a [Modifier]
 * @param content the top app bar Component (heading 과는 별개)
 * @param contentAlignment the top app bar Component Alignment
 * @param contentAlpha the top app bar Component alpha
 * @param titleVerticalArrangement the title's vertical arrangement
 * @param titleBottomPadding the title's bottom padding
 * @param hideTitleSemantics hides the title node from the semantic tree. Apply this
 * boolean when this layout is part of a [TwoRowsTopAppBar] to hide the title's semantics
 * from accessibility services. This is needed to avoid having multiple titles visible to
 * accessibility services at the same time, when animating between collapsed / expanded states.
 * @param isHeadingLayout is HeadingLayout
 * @param navigationIcon a navigation icon [Composable]
 * @param actions actions [Composable]
 */
@Composable
private fun TopAppBarLayout(
    modifier: Modifier,
    heightPx: Float,
    navigationIconContentColor: Color,
    actionIconContentColor: Color,
    content: @Composable () -> Unit,
    contentAlignment: Alignment.Horizontal,
    @FloatRange(from = 0.0, to = 1.0) contentAlpha: Float,
    titleVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
    titleBottomPadding: Int = 0,
    hideTitleSemantics: Boolean,
    isHeadingLayout: Boolean,
    navigationIcon: @Composable (() -> Unit)?,
    actions: @Composable (() -> Unit)?,
) {

}

object SooumTopAppBarDefaults {
    val TopTitleAlphaEasing = CubicBezierEasing(.8f, 0f, .8f, .15f)
    val TopAppBarComponentPadding = 8.dp
    val TopAppBarHeadingHorizontalPadding = 20.dp - TopAppBarComponentPadding
    val TopAppBarHorizontalPaddingWithIcon = 8.dp
    val TopAppBarVerticalPadding = 24.dp
    val TopAppBarPinnedHeight = 48.dp
    val TopAppBarIconHeight = 40.dp

    @Composable
    fun topAppBarColors(
        containerColor: Color = NeutralColor.WHITE,
        navigationIconContentColor: Color = NeutralColor.BLACK,
        titleContentColor: Color = NeutralColor.BLACK,
        headingContentColor: Color = NeutralColor.BLACK,
        bodyContentColor: Color = NeutralColor.BLACK,
        actionIconContentColor: Color = NeutralColor.BLACK,
    ): TopAppBarColors = DefaultTopAppBarColors(
        containerColor = containerColor,
        navigationIconContentColor = navigationIconContentColor,
        titleContentColor = titleContentColor,
        headingContentColor = headingContentColor,
        bodyContentColor = bodyContentColor,
        actionIconContentColor = actionIconContentColor
    )
}

@Stable
interface TopAppBarColors {
    @Composable
    fun containerColor(): Color

    @Composable
    fun navigationIconContentColor(): Color

    @Composable
    fun titleContentColor(): Color

    @Composable
    fun headingContentColor(): Color

    @Composable
    fun bodyContentColor(): Color

    @Composable
    fun actionIconContentColor(): Color
}

@Stable
private class DefaultTopAppBarColors constructor(
    private val containerColor: Color,
    private val navigationIconContentColor: Color,
    private val titleContentColor: Color,
    private val headingContentColor: Color,
    private val bodyContentColor: Color,
    private val actionIconContentColor: Color,
) : TopAppBarColors {
    @Composable
    override fun containerColor(): Color {
        return containerColor
    }

    @Composable
    override fun navigationIconContentColor(): Color {
        return navigationIconContentColor
    }

    @Composable
    override fun titleContentColor(): Color {
        return titleContentColor
    }

    @Composable
    override fun headingContentColor(): Color {
        return headingContentColor
    }

    @Composable
    override fun bodyContentColor(): Color {
        return bodyContentColor
    }

    @Composable
    override fun actionIconContentColor(): Color {
        return actionIconContentColor
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is DefaultTopAppBarColors) return false

        if (containerColor != other.containerColor) return false
        if (navigationIconContentColor != other.navigationIconContentColor) return false
        if (titleContentColor != other.titleContentColor) return false
        if (headingContentColor != other.titleContentColor) return false
        if (bodyContentColor != other.titleContentColor) return false
        if (actionIconContentColor != other.actionIconContentColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + navigationIconContentColor.hashCode()
        result = 31 * result + titleContentColor.hashCode()
        result = 31 * result + headingContentColor.hashCode()
        result = 31 * result + bodyContentColor.hashCode()
        result = 31 * result + actionIconContentColor.hashCode()

        return result
    }
}

/**
 * Settles the app bar by flinging, in case the given velocity is greater than zero, and snapping
 * after the fling settles.
 */
@OptIn(ExperimentalMaterial3Api::class)
private suspend fun settleAppBar(
    state: TopAppBarState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?
): Velocity {
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }
    var remainingVelocity = velocity

    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        AnimationState(
            initialValue = 0f,
            initialVelocity = velocity,
        )
            .animateDecay(flingAnimationSpec) {
                val delta = value - lastValue
                val initialHeightOffset = state.heightOffset
                state.heightOffset = initialHeightOffset + delta
                val consumed = abs(initialHeightOffset - state.heightOffset)
                lastValue = value
                remainingVelocity = this.velocity
                // avoid rounding errors and stop if anything is unconsumed
                if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
            }
    }

    if (snapAnimationSpec != null) {
        if (state.heightOffset < 0 &&
            state.heightOffset > state.heightOffsetLimit
        ) {
            AnimationState(initialValue = state.heightOffset).animateTo(
                if (state.collapsedFraction < 0.5f) {
                    0f
                } else {
                    state.heightOffsetLimit
                },
                animationSpec = snapAnimationSpec
            ) { state.heightOffset = value }
        }
    }

    return Velocity(0f, remainingVelocity)
}