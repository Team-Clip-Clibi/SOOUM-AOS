package com.phew.presentation.detail.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent
import com.phew.domain.dto.Poll
import com.phew.domain.dto.PollOption as DomainPollOption
import com.phew.presentation.detail.R
import kotlin.math.roundToInt

@Composable
internal fun CardDetailPoll(
    poll: Poll,
    modifier: Modifier = Modifier,
    isVoteLoading: Boolean = false,
    onOptionClick: (Long) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        poll.options.forEach { option ->
            if (poll.isVoted) {
                VotedPollOption(
                    option = option,
                    enabled = !isVoteLoading,
                    onClick = { onOptionClick(option.pollOptionId) }
                )
            } else {
                UnvotedPollOption(
                    option = option,
                    enabled = !isVoteLoading,
                    onClick = { onOptionClick(option.pollOptionId) }
                )
            }
        }

        Text(
            text = stringResource(
                id = R.string.card_detail_poll_voter_count,
                poll.totalVoterCount
            ),
            style = TextComponent.CAPTION_2_M_12,
            color = NeutralColor.GRAY_400
        )
    }
}

@Composable
private fun UnvotedPollOption(
    option: DomainPollOption,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = NeutralColor.GRAY_100,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = option.content,
            modifier = Modifier.weight(1f),
            style = TextComponent.SUBTITLE_1_M_16,
            color = NeutralColor.BLACK,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun VotedPollOption(
    option: DomainPollOption,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val percent = option.votePercentage.toPercent()
    val fraction = (percent / 100f).coerceIn(0f, 1f)
    val isSelected = option.isVoted
    val textStyle = if (isSelected) {
        TextComponent.TITLE_2_SB_16.copy(fontWeight = FontWeight.SemiBold)
    } else {
        TextComponent.SUBTITLE_1_M_16
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = NeutralColor.GRAY_100,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(48.dp)
                .background(
                    color = if (isSelected) Primary.LIGHT_1 else NeutralColor.GRAY_200,
                    shape = RoundedCornerShape(10.dp)
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CheckMark(
                color = if (isSelected) Primary.MAIN else NeutralColor.BLACK,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = option.content,
                modifier = Modifier.weight(1f),
                style = textStyle,
                color = NeutralColor.BLACK,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    id = R.string.card_detail_poll_option_result,
                    percent,
                    option.voteCount ?: 0L
                ),
                style = textStyle,
                color = NeutralColor.BLACK,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CheckMark(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.22f, size.height * 0.52f),
            end = Offset(size.width * 0.42f, size.height * 0.72f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.42f, size.height * 0.72f),
            end = Offset(size.width * 0.78f, size.height * 0.30f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun Double?.toPercent(): Int {
    val value = this ?: 0.0
    val normalized = if (value in 0.0..1.0) value * 100 else value
    return normalized.roundToInt().coerceIn(0, 100)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CardDetailPollVotedPreview() {
    CardDetailPoll(
        poll = Poll(
            totalVoterCount = 10,
            isVoted = true,
            options = listOf(
                DomainPollOption(1, "교촌 허니콤보", 2, 20.0, true),
                DomainPollOption(2, "BHC 뿌링클", 1, 10.0, false),
                DomainPollOption(3, "처갓집 양념치킨", 7, 70.0, false),
                DomainPollOption(4, "지코바", 0, 0.0, false)
            )
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CardDetailPollPreview() {
    CardDetailPoll(
        poll = Poll(
            totalVoterCount = 10,
            isVoted = false,
            options = listOf(
                DomainPollOption(1, "교촌 허니콤보", null, null, false),
                DomainPollOption(2, "BHC 뿌링클", null, null, false)
            )
        )
    )
}
