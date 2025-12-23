package com.phew.core_design.util.extension

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * parent padding을 무시하고 화면 넓이를 꽉 채운다.
 */
fun Modifier.fillFullWidth(parentPadding: Dp = 16.dp) = then(
    Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(
            constraints.copy(
                maxWidth = constraints.maxWidth + (parentPadding * 2).roundToPx(),
            )
        )
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
)

/**
 * Bottom ButtonPadding 을 적용한다.
 */
fun Modifier.bottomButtonPadding(): Modifier {
    return this.padding(
        horizontal = 16.dp,
        vertical = 10.dp
    )
}
