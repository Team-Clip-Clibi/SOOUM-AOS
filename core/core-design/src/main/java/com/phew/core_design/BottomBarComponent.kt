package com.phew.core_design

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

object BottomBarComponent {
    private const val HOME_VIEW: Int = 1
    private const val CARD_VIEW: Int = 2
    private const val TAG_VIEW: Int = 3
    private const val MY_PROFILE_VIEW: Int = 4

    @Composable
    fun HomeBottomBar(
        homeClick: () -> Unit,
        addCardClick: () -> Unit,
        tagClick: () -> Unit,
        myProfileClick: () -> Unit,
    ) {
        var selectIndex by remember { mutableIntStateOf(HOME_VIEW) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .background(color = NeutralColor.WHITE)
                .border(width = 1.dp, color = NeutralColor.GRAY_200)
                .padding(start = 16.dp, bottom = 8.dp, top = 8.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier
                    .width(76.dp)
                    .height(46.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        selectIndex = HOME_VIEW
                        homeClick()
                    },
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_home_filled),
                    contentDescription = "home",
                    colorFilter = if (selectIndex == HOME_VIEW) ColorFilter.tint(NeutralColor.BLACK) else ColorFilter.tint(
                        NeutralColor.GRAY_300
                    )
                )
                Text(
                    text = stringResource(R.string.bottom_view_home),
                    style = TextComponent.CAPTION_1_SB_12,
                    color = if (selectIndex == HOME_VIEW) NeutralColor.BLACK else NeutralColor.GRAY_300
                )
            }
            Column(
                modifier = Modifier
                    .width(76.dp)
                    .height(46.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        selectIndex = CARD_VIEW
                        addCardClick()
                    },
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = if (selectIndex == CARD_VIEW) painterResource(R.drawable.ic_plus_filled) else painterResource(
                        R.drawable.ic_plus_filled_gray
                    ),
                    contentDescription = "add card",
                    modifier = Modifier
                        .size(22.dp)
                )
                Text(
                    text = stringResource(R.string.bottom_view_add_card),
                    style = TextComponent.CAPTION_1_SB_12,
                    color = if (selectIndex == CARD_VIEW) NeutralColor.BLACK else NeutralColor.GRAY_300
                )
            }
            Column(
                modifier = Modifier
                    .width(76.dp)
                    .height(46.dp)
                    .clickable (
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ){
                        selectIndex = TAG_VIEW
                        tagClick()
                    },
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = if (selectIndex == TAG_VIEW) painterResource(R.drawable.ic_tag_filled) else painterResource(
                        R.drawable.ic_tag_filled_gray
                    ),
                    contentDescription = "tag"

                )
                Text(
                    text = stringResource(R.string.bottom_view_tag),
                    style = TextComponent.CAPTION_1_SB_12,
                    color = if (selectIndex == TAG_VIEW) NeutralColor.BLACK else NeutralColor.GRAY_300
                )
            }
            Column(
                modifier = Modifier
                    .width(76.dp)
                    .height(46.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        selectIndex = MY_PROFILE_VIEW
                        myProfileClick()
                    },
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_user_filled),
                    contentDescription = "my profile",
                    colorFilter = if (selectIndex == MY_PROFILE_VIEW) ColorFilter.tint(NeutralColor.BLACK) else ColorFilter.tint(
                        NeutralColor.GRAY_300
                    )
                )
                Text(
                    text = stringResource(R.string.bottom_view_my),
                    style = TextComponent.CAPTION_1_SB_12,
                    color = if (selectIndex == MY_PROFILE_VIEW) NeutralColor.BLACK else NeutralColor.GRAY_300
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    BottomBarComponent.HomeBottomBar(
        homeClick = {},
        addCardClick = {},
        tagClick = {},
        myProfileClick = {}
    )
}