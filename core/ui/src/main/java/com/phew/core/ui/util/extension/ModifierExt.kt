package com.phew.core.ui.util.extension

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll

@Composable
fun Modifier.nestedScrollWithStickyHeader(scrollState: ScrollState): Modifier {
    return this.nestedScroll(
        remember {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    return if (available.y > 0) Offset.Zero else Offset(
                        x = 0f,
                        y = -scrollState.dispatchRawDelta(-available.y)
                    )
                }
            }
        }
    )

}