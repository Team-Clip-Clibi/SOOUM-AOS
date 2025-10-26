package com.phew.core_design.component.filter

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent


@Composable
fun SooumFilter(
    modifier: Modifier,
    selectedFilter: String,
    filters: List<String>,
    onFilterSelected: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FilterDesignTokens.BackgroundDefault)
            .padding(vertical = FilterDesignTokens.VerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEach { label ->
            FilterItem(
                label = label,
                selected = label == selectedFilter,
                onClick = { onFilterSelected(label) }
            )
        }
    }
}

// ===== FilterItem.kt =====
@Composable
private fun FilterItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        if (selected) FilterDesignTokens.BackgroundSelectedColor else FilterDesignTokens.BackgroundDefault,
        label = "bgAnim"
    )

    val textColor by animateColorAsState(
        if (selected) FilterDesignTokens.TextSelectedColor else FilterDesignTokens.TextDefaultColor,
        label = "textAnim"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(FilterDesignTokens.BorderRadius))
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(FilterDesignTokens.BorderRadius)
    ) {
        Text(
            text = label,
            color = textColor,
            style = TextComponent.SUBTITLE_3_SB_14,
            modifier = Modifier.padding(
                horizontal = FilterDesignTokens.PaddingHorizontal,
                vertical = FilterDesignTokens.PaddingVertical
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterPreview() {
    var selected by remember { mutableStateOf("컬러") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Filter.item", fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Default", color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                FilterItem("Label", selected = false, onClick = {})
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Selected", color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                FilterItem("Label", selected = true, onClick = {})
            }
        }

        Text("Filter", fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))

        SooumFilter (
            modifier = Modifier,
            filters = listOf("컬러", "자연", "감성", "푸드", "추상", "메모"),
            selectedFilter = selected,
            onFilterSelected = { selected = it }
        )
    }
}



object FilterDesignTokens {
    val BackgroundSelectedColor = NeutralColor.GRAY_100
    val TextSelectedColor = NeutralColor.GRAY_600

    val BackgroundDefault = NeutralColor.WHITE
    val TextDefaultColor = NeutralColor.GRAY_400

    val HorizontalPadding = 16.dp
    val VerticalPadding = 9.5.dp

    val BorderRadius = 8.dp

    val PaddingHorizontal = 12.dp
    val PaddingVertical = 8.dp
}