package com.phew.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.phew.core_design.NeutralColor
import com.phew.domain.dto.FollowData
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
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

}