package com.phew.core_design.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import com.phew.core_design.component.SooumTabRowDefaults.contentEnabledColor
import com.phew.core_design.component.SooumTabRowDefaults.contentSelectedColor
import com.phew.core_design.theme.SooumTheme
import kotlin.math.max

data class TabSelectType(
    val tabTitle: String,
    val disabled: Boolean,
)

@Composable
fun SooumTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable () -> Unit,
    selectedContentColor: Color = contentSelectedColor,
    unselectedContentColor: Color = contentEnabledColor,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    SooumTheme {
        SooumTabLayout(
            selected,
            onClick,
            modifier,
            enabled,
            selectedContentColor,
            unselectedContentColor,
            interactionSource
        ) {
            TabBaselineLayout(text = text)
        }
    }
}

@Composable
fun SooumTabLayout(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    selectedContentColor: Color = contentSelectedColor,
    unselectedContentColor: Color = contentEnabledColor,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit
) {
    // The color of the Ripple should always the selected color, as we want to show the color
    // before the item is considered selected, and hence before the new contentColor is
    // provided by TabTransition.
    TabTransition(
        activeColor = selectedContentColor,
        disableColor = unselectedContentColor,
        selected = selected
    ) {
        Column(
            modifier = modifier
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    enabled = enabled,
                    role = Role.Tab,
                    interactionSource = interactionSource,
                    indication = null
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content
        )
    }
}


/**
 *  Tab 선택 여부에 따른 색상 설정
 */
@Composable
private fun TabTransition(
    activeColor: Color,
    disableColor: Color,
    selected: Boolean,
    content: @Composable () -> Unit
) {
    val transition = updateTransition(selected, label = "")
    val color by transition.animateColor(
        transitionSpec = {
            if (false isTransitioningTo true) {
                tween(
                    durationMillis = TabFadeInAnimationDuration,
                    delayMillis = TabFadeInAnimationDelay,
                    easing = LinearEasing
                )
            } else {
                tween(
                    durationMillis = TabFadeOutAnimationDuration,
                    easing = LinearEasing
                )
            }
        }, label = ""
    ) {
        if (it) {
            activeColor
        } else {
            disableColor
        }
    }
    CompositionLocalProvider(
        LocalContentColor provides color,
        content = content
    )
}

@Composable
private fun TabBaselineLayout(
    text: @Composable (() -> Unit)
) {
    SooumTheme {
        Layout(
            {
                Box(
                    Modifier
                        .layoutId("text")
                        .padding(start = 16.dp, end = 16.dp)
                        .widthIn(max = TabMaxWidth),
                    contentAlignment = Alignment.Center
                ) { text() }
            }
        ) { measurables, constraints ->
            val textPlaceable = text.let {
                measurables.fastFirst { it.layoutId == "text" }.measure(
                    // Measure with loose constraints for height as we don't want the text to take up more
                    // space than it needs
                    constraints.copy(minHeight = 0)
                )
            }

            val tabWidth = textPlaceable.measuredWidth ?: 0
            val specHeight = TabHeight.roundToPx()

            val tabHeight = max(
                specHeight,
                (textPlaceable.height ?: 0)
            )

            layout(tabWidth, tabHeight) {
                placeText(textPlaceable, tabHeight)
            }
        }
    }
}

private fun Placeable.PlacementScope.placeText(
    textOrIconPlaceable: Placeable,
    tabHeight: Int
) {
    val contentY = (tabHeight - textOrIconPlaceable.height) / 2
    textOrIconPlaceable.placeRelative(0, contentY)
}

private val TabHeight = 40.dp
private val TabMaxWidth = 200.dp

// Tab transition specifications
private const val TabFadeInAnimationDuration = 150
private const val TabFadeInAnimationDelay = 100
private const val TabFadeOutAnimationDuration = 100


@Preview(showBackground = true, name = "TabRow with Indicators")
@Composable
private fun TabRowWithIndicatorsPreview() {
    var selectedTab by androidx.compose.runtime.remember { mutableIntStateOf(1) }

    val tabs = listOf(
        TabSelectType("최신카드", false),
        TabSelectType("인기카드", false),
        TabSelectType("주변카드", false)
    )

    SooumTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text(
                text = "TabRow Preview",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
            )

            // With Indicator
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "With Indicator",
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                )
                SooumTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    tabs.forEachIndexed { index, tab ->
                        SooumTab(
                            selected = selectedTab == index,
                            onClick = {
                                if (!tab.disabled) {
                                    selectedTab = index
                                }
                            },
                            enabled = !tab.disabled,
                            text = {
                                Text(
                                    text = tab.tabTitle,
                                    color = LocalContentColor.current
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

