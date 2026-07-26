package com.phew.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Constraints
import coil3.compose.AsyncImage
import com.phew.core_design.NeutralColor
import com.phew.domain.dto.FollowData
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.phew.core_design.SmallButton
import com.phew.core_design.TabBar
import com.phew.core_design.TextComponent
import com.phew.profile.R

object ProfileComponent {
    @Composable
    fun FollowView(
        data: FollowData,
        onClick: (Long) -> Unit,
        onShowProfile: (Pair<String, Long>) -> Unit,
        isGrayColor: Boolean,
        isButtonShow: Boolean,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(color = NeutralColor.WHITE)
                .clickable(
                    onClick = { onShowProfile(Pair(data.nickname, data.memberId)) },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 10.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = if (data.profileImageUrl.isNullOrEmpty()) com.phew.core_design.R.drawable.ic_profile else data.profileImageUrl,
                contentDescription = "${data.nickname} profileImage",
                modifier = Modifier
                    .size(36.dp)
                    .border(
                        width = 1.dp,
                        color = NeutralColor.GRAY_300,
                        shape = RoundedCornerShape(size = 100.dp)
                    )
                    .clip(shape = RoundedCornerShape(size = 100.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                text = data.nickname,
                style = TextComponent.SUBTITLE_2_SB_14,
                color = NeutralColor.GRAY_600,
                modifier = Modifier.weight(1f)
            )
            if (isButtonShow) {
                SmallButton.NoIconPrimary(
                    baseColor = if (isGrayColor) NeutralColor.GRAY_100 else NeutralColor.BLACK,
                    onClick = {
                        onClick(data.memberId)
                    },
                    buttonText = if (isGrayColor) stringResource(R.string.follow_btn_following) else stringResource(
                        R.string.follow_btn_follow
                    ),
                    textColor = if (isGrayColor) NeutralColor.GRAY_600 else NeutralColor.WHITE,
                    modifier = Modifier.width(68.dp)
                )
            }
        }
    }

    @Composable
    fun CardTabView(
        selectIndex: Int,
        onFeedCardClick: () -> Unit,
        onCommentCardClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = NeutralColor.WHITE)
                .padding(top = 16.dp)
        ) {
            TabBar.TabBarTwo(
                data = listOf(
                    stringResource(R.string.profile_txt_card),
                    stringResource(R.string.profile_txt_comment_card)
                ),
                selectTabData = selectIndex,
                onFirstItemClick = onFeedCardClick,
                onSecondItemClick = onCommentCardClick
            )
        }
    }

    @Composable
    fun CardFollowerView(
        title: String,
        data: String,
        onClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .width(72.dp)
                .height(64.dp)
                .padding(top = 8.dp, bottom = 8.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = title,
                style = TextComponent.BODY_1_M_14,
                color = NeutralColor.GRAY_500
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = data,
                style = TextComponent.TITLE_1_SB_18,
                color = NeutralColor.BLACK
            )
        }
    }

    @Composable
    fun BioView(
        bio: String,
        modifier: Modifier = Modifier,
    ) {
        val bioText = bio.trim()
        if (bioText.isEmpty()) return
        var expanded by remember(bioText) { mutableStateOf(false) }
        val moreText = stringResource(R.string.profile_txt_bio_more)
        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current

        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 76.dp, bottom = 12.dp)
        ) {
            val widthPx = with(density) { maxWidth.roundToPx() }
            val collapsedPrefix = remember(bioText, moreText, widthPx) {
                if (widthPx <= 0) {
                    null
                } else {
                    resolveCollapsedBioPrefix(
                        bioText = bioText,
                        moreText = moreText,
                        measure = { text ->
                            textMeasurer.measure(
                                text = text,
                                style = TextComponent.BODY_1_M_14,
                                maxLines = PROFILE_BIO_COLLAPSED_MAX_LINES,
                                overflow = TextOverflow.Clip,
                                constraints = Constraints(maxWidth = widthPx)
                            ).hasVisualOverflow
                        }
                    )
                }
            }
            val isCollapsed = !expanded && collapsedPrefix != null
            Text(
                text = if (isCollapsed) {
                    buildCollapsedBioText(
                        prefix = collapsedPrefix.orEmpty(),
                        moreText = moreText
                    )
                } else {
                    AnnotatedString(bioText)
                },
                style = TextComponent.BODY_1_M_14,
                color = NeutralColor.BLACK,
                maxLines = if (expanded) Int.MAX_VALUE else PROFILE_BIO_COLLAPSED_MAX_LINES,
                overflow = TextOverflow.Clip,
                modifier = Modifier
                    .let { baseModifier ->
                        if (isCollapsed) {
                            baseModifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { expanded = true }
                            )
                        } else {
                            baseModifier
                        }
                    }
            )
        }
    }

}

private fun resolveCollapsedBioPrefix(
    bioText: String,
    moreText: String,
    measure: (AnnotatedString) -> Boolean,
): String? {
    if (!measure(AnnotatedString(bioText))) return null

    var low = 0
    var high = bioText.length
    var best = ""
    while (low <= high) {
        val mid = (low + high) / 2
        val prefix = bioText.take(mid).trimEnd()
        val hasOverflow = measure(buildCollapsedBioText(prefix = prefix, moreText = moreText))
        if (hasOverflow) {
            high = mid - 1
        } else {
            best = prefix
            low = mid + 1
        }
    }
    return best.trimEnd('.', '…', ' ')
}

private fun buildCollapsedBioText(
    prefix: String,
    moreText: String,
): AnnotatedString {
    return buildAnnotatedString {
        append(prefix)
        append("... ")
        withStyle(
            SpanStyle(
                color = NeutralColor.GRAY_400,
                fontWeight = FontWeight(600)
            )
        ) {
            append(moreText)
        }
    }
}

private const val PROFILE_BIO_COLLAPSED_MAX_LINES = 4
