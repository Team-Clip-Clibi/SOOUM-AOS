package com.phew.core_design.component.filed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties.Text
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.OpacityColor
import com.phew.core_design.TextComponent
import com.phew.core_design.component.filed.FiledDesignTokens.CommentMinHeight
import com.phew.core_design.component.filed.FiledDesignTokens.CommentWidth
import com.phew.core_design.component.filed.FiledDesignTokens.CornerRadius
import com.phew.core_design.component.filed.FiledDesignTokens.HorizontalPadding
import com.phew.core_design.component.filed.FiledDesignTokens.MainMinHeight
import com.phew.core_design.component.filed.FiledDesignTokens.MainWidth
import com.phew.core_design.component.filed.FiledDesignTokens.VerticalPadding

object FiledDesignTokens{
    val HorizontalPadding = 24.dp
    val VerticalPadding = 20.dp

    val CommentWidth = 163.dp
    val CommentMinHeight = 53.dp

    val MainWidth = 279.dp
    val MainMinHeight = 61.dp

    val CornerRadius = 12.dp

    val BorderBackground = NeutralColor.GRAY_200

    val FieldTextTint = NeutralColor.WHITE
}

enum class CardFieldType {
    MAIN, COMMENT
}

/**
 *  TODO 디자인 시스템 질문 하고 추가 작업이 필요해보임
 */
@Composable
fun CardField(
    modifier: Modifier = Modifier,
    cardFieldType: CardFieldType,
    text: String,
    fontFamily: FontFamily
) {
    Box(
        modifier = modifier
            .width(if (cardFieldType == CardFieldType.MAIN) MainWidth else CommentWidth)
            .heightIn(min = if(cardFieldType == CardFieldType.MAIN) MainMinHeight else CommentMinHeight)
            .background(
                color = OpacityColor.blackSmallColor,
                shape = RoundedCornerShape(CornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        when (cardFieldType) {
            CardFieldType.MAIN -> {
                MainCardField(
                    text = text,
                    fontFamily = fontFamily
                )
            }
            CardFieldType.COMMENT -> {
                CommentField(
                    text = text,
                    fontFamily = fontFamily
                )
            }
        }
    }
}

/**
 *  TODO Scrollable 필요 해보임
 */
@Composable
private fun MainCardField(
    text: String,
    fontFamily: FontFamily
) {
    Text(
        text = text,
        style = TextComponent.BODY_1_M_14,
        color = NeutralColor.WHITE,
        fontFamily = fontFamily,
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun CommentField(
    text: String,
    fontFamily: FontFamily
) {
    Text(
        text = text,
        style = TextComponent.BODY_1_M_14,
        color = NeutralColor.WHITE,
        fontFamily = fontFamily,
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
    )
}