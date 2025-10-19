package com.phew.core_design.component.tab

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFold
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.phew.core_design.NeutralColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SooumTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    containerColor: Color = SooumTabRowDefaults.containerColor,
    contentColor: Color = SooumTabRowDefaults.contentSelectedColor,
    edgePadding: Dp = SooumTabRowDefaults.ScrollableTabRowEdgeStartPadding,
    indicator: @Composable (tabPositions: List<SooumTabPosition>) -> Unit = @Composable { tabPositions ->
        if (selectedTabIndex < tabPositions.size) {
            val currentTab = tabPositions[selectedTabIndex]
            val contentWidth by animateDpAsState(
                targetValue = currentTab.contentWidth,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                label = ""
            )
            val contentOffset by animateDpAsState(
                targetValue = currentTab.left + (currentTab.width - currentTab.contentWidth) / 2,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                label = ""
            )
            SooumTabRowDefaults.PrimaryIndicator(
                Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.BottomStart)
                    .offset(x = contentOffset)
                    .width(contentWidth),
                width = contentWidth,
                height = 2.dp
            )
        }
    },
    divider: @Composable () -> Unit = @Composable {
        HorizontalDivider(
            color = NeutralColor.GRAY_200,
            thickness = SooumTabRowDefaults.ActiveIndicatorHeight
        )
    },
    tabs: @Composable () -> Unit
) {
    SooumScrollableTabRowImpl(
        selectedTabIndex = selectedTabIndex,
        indicator = indicator,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        edgePadding = edgePadding,
        divider = divider,
        tabs = tabs,
        scrollState = scrollState
    )
}

@Composable
private fun SooumScrollableTabRowImpl(
    selectedTabIndex: Int,
    indicator: @Composable (tabPositions: List<SooumTabPosition>) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = SooumTabRowDefaults.containerColor,
    contentColor: Color = SooumTabRowDefaults.contentSelectedColor,
    edgePadding: Dp = SooumTabRowDefaults.ScrollableTabRowEdgeStartPadding,
    divider: @Composable () -> Unit = @Composable {
        HorizontalDivider(
            modifier = modifier.fillMaxWidth(),
            color = NeutralColor.GRAY_400
        )
    },
    tabs: @Composable () -> Unit,
    scrollState: ScrollState,
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor
    ) {
        val coroutineScope = rememberCoroutineScope()
        val scrollableTabData = remember(scrollState, coroutineScope) {
            ScrollableTabData(
                scrollState = scrollState,
                coroutineScope = coroutineScope
            )
        }
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            //  Tab Indicator line fill max width
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .background(NeutralColor.GRAY_200)
                    .fillMaxWidth()
            )

            SubcomposeLayout(
                Modifier
                    .fillMaxWidth()
                    .wrapContentSize(align = Alignment.CenterStart)
                    .horizontalScroll(scrollState)
                    .selectableGroup()
                    .clipToBounds()
            ) { constraints ->
                val minTabWidth = ScrollableTabRowMinimumTabWidth.roundToPx()
                val padding = edgePadding.roundToPx()

                val tabMeasurables = subcompose(TabSlots.Tabs, tabs)

                val layoutHeight = 56.dp.roundToPx()

                val tabConstraints = constraints.copy(
                    minWidth = minTabWidth,
                    minHeight = layoutHeight,
                    maxHeight = layoutHeight,
                )

                val tabPlaceables = mutableListOf<Placeable>()
                val tabContentWidths = mutableListOf<Dp>()
                tabMeasurables.fastForEach {
                    val placeable = it.measure(tabConstraints)
                    val contentWidth = it.maxIntrinsicWidth(placeable.height).toDp()
                    tabPlaceables.add(placeable)
                    tabContentWidths.add(contentWidth)
                }

                val layoutWidth =
                    tabPlaceables.fastFold(initial = padding * 2) { curr, measurable ->
                        curr + measurable.width
                    }

                // Position the children.
                layout(layoutWidth, layoutHeight) {
                    // Place the tabs
                    val tabPositions = mutableListOf<SooumTabPosition>()
                    var left = padding
                    tabPlaceables.fastForEachIndexed { index, placeable ->
                        placeable.placeRelative(left, 0)

                        tabPositions.add(
                            SooumTabPosition(
                                left = left.toDp(),
                                width = placeable.width.toDp(),
                                contentWidth = tabContentWidths[index]
                            )
                        )
                        left += placeable.width
                    }

                    // The divider is measured with its own height, and width equal to the total width
                    // of the tab row, and then placed on top of the tabs.
                    subcompose(TabSlots.Divider, divider).fastForEach {
                        val placeable = it.measure(
                            constraints.copy(
                                minHeight = 0,
                                minWidth = layoutWidth,
                                maxWidth = layoutWidth
                            )
                        )
                        placeable.placeRelative(0, layoutHeight - placeable.height)
                    }

                    // The indicator container is measured to fill the entire space occupied by the tab
                    // row, and then placed on top of the divider.
                    subcompose(TabSlots.Indicator) {
                        indicator(tabPositions)
                    }.fastForEach {
                        it.measure(Constraints.fixed(layoutWidth, layoutHeight)).placeRelative(0, 0)
                    }

                    scrollableTabData.onLaidOut(
                        density = this@SubcomposeLayout,
                        edgeOffset = padding,
                        tabPositions = tabPositions,
                        selectedTab = selectedTabIndex
                    )
                }
            }
        }
    }
}

@Immutable
class SooumTabPosition internal constructor(val left: Dp, val width: Dp, val contentWidth: Dp) {

    val right: Dp get() = left + width

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SooumTabPosition) return false

        if (left != other.left) return false
        if (width != other.width) return false
        if (contentWidth != other.contentWidth) return false

        return true
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + width.hashCode()
        result = 31 * result + contentWidth.hashCode()
        return result
    }

    override fun toString(): String {
        return "TabPosition(left=$left, right=$right, width=$width, contentWidth=$contentWidth)"
    }
}

object SooumTabRowDefaults {
    /**
     * The default padding from the starting edge before a tab in a [ScrollableTabRow].
     */
    val ScrollableTabRowEdgeStartPadding = 16.dp

    val ActiveIndicatorHeight = 2.0.dp

    /** Default container color of a tab row. */
    @Deprecated(
        message = "Use TabRowDefaults.primaryContainerColor instead",
        replaceWith = ReplaceWith("primaryContainerColor")
    )
    val containerColor: Color
        @Composable get() =
            NeutralColor.WHITE


    /** Default content color of a tab row. */
    @Deprecated(
        message = "Use TabRowDefaults.primaryContentColor instead",
        replaceWith = ReplaceWith("primaryContentColor")
    )
    val contentSelectedColor: Color
        @Composable get() =
            NeutralColor.BLACK

    val contentEnabledColor: Color
        @Composable get() =
            NeutralColor.GRAY_400


    /**
     * Default indicator, which will be positioned at the bottom of the [TabRow], on top of the
     * divider.
     *
     * @param modifier modifier for the indicator's layout
     * @param height height of the indicator
     * @param color color of the indicator
     */
    @Composable
    @Deprecated(
        message = "Use SecondaryIndicator instead.",
        replaceWith = ReplaceWith(
            "SecondaryIndicator(modifier, height, color)"
        )
    )
    fun Indicator(
        modifier: Modifier = Modifier,
        height: Dp = ActiveIndicatorHeight,
        color: Color = NeutralColor.BLACK
    ) {
        Box(
            modifier
                .fillMaxWidth()
                .height(height)
                .padding(bottom = 2.dp)
        )
    }

    @Composable
    fun PrimaryIndicator(
        modifier: Modifier = Modifier,
        width: Dp = 8.dp,
        height: Dp = ActiveIndicatorHeight,
        color: Color = NeutralColor.BLACK
    ) {
        Spacer(
            modifier
                .requiredSize(width, height)
                .background(color = color)
        )
    }


    @Composable
    fun SecondaryIndicator(
        modifier: Modifier = Modifier,
        height: Dp = ActiveIndicatorHeight,
        color: Color = NeutralColor.BLACK
    ) {
        Box(
            modifier
                .fillMaxWidth()
                .height(height)
                .background(color = color)
        )
    }
}

private val ScrollableTabRowMinimumTabWidth = 0.dp
private val IndicationHorizonPadding = 12.dp

private class ScrollableTabData(
    private val scrollState: ScrollState,
    private val coroutineScope: CoroutineScope
) {
    private var selectedTab: Int? = null

    fun onLaidOut(
        density: Density,
        edgeOffset: Int,
        tabPositions: List<SooumTabPosition>,
        selectedTab: Int
    ) {
        // Animate if the new tab is different from the old tab, or this is called for the first
        // time (i.e selectedTab is `null`).
        if (this.selectedTab != selectedTab) {
            this.selectedTab = selectedTab
            tabPositions.getOrNull(selectedTab)?.let {
                // Scrolls to the tab with [tabPosition], trying to place it in the center of the
                // screen or as close to the center as possible.
                val calculatedOffset = it.calculateTabOffset(density, edgeOffset, tabPositions)

                if (scrollState.value != calculatedOffset) {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(
                            calculatedOffset,
                            animationSpec = ScrollableTabRowScrollSpec
                        )
                    }
                }
            }
        }
    }

    /**
     * @return the offset required to horizontally center the tab inside this TabRow.
     * If the tab is at the start / end, and there is not enough space to fully centre the tab, this
     * will just clamp to the min / max position given the max width.
     */
    private fun SooumTabPosition.calculateTabOffset(
        density: Density,
        edgeOffset: Int,
        tabPositions: List<SooumTabPosition>
    ): Int = with(density) {
        val totalTabRowWidth = tabPositions.last().right.roundToPx() + edgeOffset
        val visibleWidth = totalTabRowWidth - scrollState.maxValue
        val tabOffset = left.roundToPx()
        val scrollerCenter = visibleWidth / 2
        val tabWidth = width.roundToPx()
        val centeredTabOffset = tabOffset - (scrollerCenter - tabWidth / 2)
        // How much space we have to scroll. If the visible width is <= to the total width, then
        // we have no space to scroll as everything is always visible.
        val availableSpace = (totalTabRowWidth - visibleWidth).coerceAtLeast(0)
        return centeredTabOffset.coerceIn(0, availableSpace)
    }
}


/**
 * [AnimationSpec] used when scrolling to a tab that is not fully visible.
 */
private val ScrollableTabRowScrollSpec: AnimationSpec<Float> = tween(
    durationMillis = 250,
    easing = FastOutSlowInEasing
)


private enum class TabSlots {
    Tabs,
    Divider,
    Indicator
}