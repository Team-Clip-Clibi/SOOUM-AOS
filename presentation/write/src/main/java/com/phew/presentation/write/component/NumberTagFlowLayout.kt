package com.phew.presentation.write.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.component.card.BaseCardData
import com.phew.core_design.component.card.CardView
import com.phew.core_design.component.tag.Tag
import com.phew.core_design.component.tag.TagState
import com.phew.core_design.theme.SooumTheme


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NumberTagFlowLayout(
    tags: List<NumberTagItem>,
    modifier: Modifier = Modifier,
    onTagClick: (NumberTagItem) -> Unit = {}
) {
    if (tags.isEmpty()) return

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .background(NeutralColor.GRAY_100)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            Tag(
                state = TagState.Number,
                text = tag.name,
                number = tag.countLabel,
                numberValue = tag.countValue,
                numberUnit = tag.countUnit,
                onClick = { onTagClick(tag) }
            )
        }
    }
}

@Immutable
data class NumberTagItem(
    val id: String,
    val name: String,
    val countLabel: String,
    val countValue: String = countLabel,
    val countUnit: String = ""
)

@Preview(showBackground = true)
@Composable
private fun NumberTagFlowLayoutPreview() {
    SooumTheme {
        NumberTagFlowLayout(
            tags = listOf(
                NumberTagItem(id = "1", name = "숨", countLabel = "120"),
                NumberTagItem(id = "2", name = "산책", countLabel = "80"),
                NumberTagItem(id = "3", name = "호수", countLabel = "45"),
                NumberTagItem(id = "4", name = "챌린지", countLabel = "999+"),
                NumberTagItem(id = "5", name = "러닝", countLabel = "1.2")
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NumberTagFlowLayoutWithCardPreview() {
    SooumTheme {
        Column(
            modifier = Modifier
                .height(328.dp)
                .width(328.dp)
                .background(NeutralColor.WHITE)
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.ime.only(WindowInsetsSides.Bottom)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardView(
                data = BaseCardData.Write(
                    content = "숨 속에 가득한 이야기들을 전해요.",
                    tags = listOf("숨", "산책"),
                    onAddTag = {},
                    onRemoveTag = {}
                )
            )
            NumberTagFlowLayout(
                tags = listOf(
                    NumberTagItem(id = "1", name = "숨", countLabel = "120"),
                    NumberTagItem(id = "2", name = "힐링", countLabel = "80"),
                    NumberTagItem(id = "3", name = "감성", countLabel = "45"),
                    NumberTagItem(id = "4", name = "주말", countLabel = "999+"),
                    NumberTagItem(id = "5", name = "산책길", countLabel = "1.2")
                )
            )
        }
    }
}
