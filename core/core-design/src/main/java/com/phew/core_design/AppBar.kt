package com.phew.core_design

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import com.phew.core_common.BANNER_NEWS
import com.phew.core_common.BANNER_SERVICE
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AppBar {
    @Composable
    fun HomeAppBar(newAlarm: Boolean, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(color = NeutralColor.WHITE)
                .zIndex(1f)
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_sooum_black),
                contentDescription = "sooum logo",
                modifier = Modifier
                    .width(100.dp)
                    .height(17.dp)
            )

            Icon(
                painter = if (newAlarm) painterResource(R.drawable.ic_bell_stoke) else painterResource(
                    R.drawable.ic_bell_no_badge_stoke
                ),
                contentDescription = if (newAlarm) "new alarm" else "no new alarm",
                modifier = Modifier
                    .size(48.dp)
                    .padding(12.dp)
                    .clickable {
                        onClick()
                    }
            )
        }
    }

    @Composable
    fun IconBothAppBar(
        @DrawableRes startImage: Int = R.drawable.ic_left,
        @DrawableRes endImage: Int = R.drawable.ic_home_stoke,
        appBarText: String = "Title",
        startClick: () -> Unit,
        endClick: () -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(color = NeutralColor.WHITE)
                .padding(start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { startClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(startImage),
                    contentDescription = "left icon",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(6.dp)
                )
            }
            Text(
                text = appBarText,
                style = TextComponent.TITLE_1_SB_18,
                color = NeutralColor.BLACK
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { endClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(endImage),
                    contentDescription = "right icon",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    @Composable
    fun IconLeftAppBar(
        @DrawableRes image: Int = R.drawable.ic_left,
        onClick: () -> Unit,
        appBarText: String = "Title",
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(48.dp)
                .statusBarsPadding()
                .background(NeutralColor.WHITE)
                .padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(image),
                    contentDescription = "left icon",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(6.dp)
                )
            }
            Text(
                text = appBarText,
                style = TextComponent.TITLE_1_SB_18,
                color = NeutralColor.BLACK,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(48.dp))
        }
    }

    /**
     *  TODO 추후 TopAppBar 에 통합 예정
     */
    @Composable
    fun TextButtonAppBar(
        @DrawableRes image: Int = R.drawable.ic_left,
        onClick: () -> Unit,
        appBarText: String = "Title",
        buttonText: String = "Button",
        onButtonClick: () -> Unit,
        buttonTextStyle: TextStyle = TextComponent.SUBTITLE_1_M_16,
        buttonTextColor: Color = NeutralColor.GRAY_400
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(48.dp)
                .background(NeutralColor.WHITE),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(AppBarDefaults.IconSlotSize)
                    .fillMaxHeight()
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(image),
                    contentDescription = "back",
                    modifier = Modifier.size(20.dp),
                    tint = NeutralColor.BLACK
                )
            }

            Text(
                text = appBarText,
                style = TextComponent.TITLE_1_SB_18,
                color = NeutralColor.BLACK,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .width(AppBarDefaults.IconSlotSize)
                    .fillMaxHeight()
                    .clickable { onButtonClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonText,
                    style = buttonTextStyle.copy(color = buttonTextColor)
                )
            }
        }
    }

    @Composable
    fun HomeBanner(data: List<Triple<String, String, String>>) {
        val pageState = rememberPagerState(pageCount = { data.size })
        LaunchedEffect(Unit) {
            while (true) {
                delay(3000)
                val nextPage = (pageState.currentPage + 1) % data.size
                launch {
                    pageState.animateScrollToPage(nextPage)
                }
            }
        }
        Column {
            HorizontalPager(
                state = pageState,
                userScrollEnabled = true,
            ) { page ->
                Banner(data = data, currentPage = page)
            }
        }
    }
}

private object AppBarDefaults {
    val IconSlotSize = 56.dp
}

/**
 * data: List<Pair<String, String , String>> 첫번째 인자가 분류 두번째가 제목 세번째가 내용
 */
@Composable
private fun Banner(data: List<Triple<String, String, String>>, currentPage: Int) {
    if (data.isEmpty()) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(71.dp)
            .background(color = NeutralColor.WHITE, shape = RoundedCornerShape(size = 16.dp))
            .border(
                width = 1.dp,
                color = NeutralColor.GRAY_100,
                shape = RoundedCornerShape(size = 16.dp)
            )
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = if (data[currentPage].first == BANNER_NEWS) painterResource(R.drawable.ic_mail_filled_bule) else painterResource(
                    R.drawable.ic_settings_filled
                ),
                contentDescription = data[currentPage].second + data[currentPage].third,
                modifier = Modifier
                    .size(28.dp)
                    .padding(start = 4.dp, top = 4.5.dp, end = 4.dp, bottom = 3.5.dp),
                colorFilter = if (data[currentPage].first == BANNER_SERVICE) {
                    ColorFilter.tint(NeutralColor.GRAY_400)
                } else {
                    null
                }
            )
            Column {
                Text(
                    text = data[currentPage].second,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_400,
                )
                Text(
                    text = data[currentPage].third,
                    style = TextComponent.SUBTITLE_3_SB_14,
                    color = NeutralColor.GRAY_600
                )
            }
        }
        PageIndicator(
            numberOfPage = data.size,
            selectPage = currentPage,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Composable
private fun PageIndicator(
    numberOfPage: Int,
    modifier: Modifier = Modifier,
    selectPage: Int = 0
) {
    if (numberOfPage <= 0) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = modifier.padding(top = 2.dp, end = 2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(numberOfPage) { index ->
                Indicator(isSelected = index == selectPage)
            }
        }
    }
}


@Composable
private fun Indicator(isSelected: Boolean) {
    val width by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 4.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = ""
    )
    Box(
        modifier = Modifier
            .width(width)
            .height(4.dp)
            .clip(CircleShape)
            .background(color = if (isSelected) NeutralColor.GRAY_600 else NeutralColor.GRAY_300)
    )
}

@Composable
@Preview
private fun Preview() {
    val dummyData: List<Triple<String, String, String>> = listOf(
        Triple(BANNER_NEWS, "테스트 제목1", "테스트 내용1"),
        Triple(BANNER_SERVICE, "테스트 제목2", "테스트 내용2"),
        Triple(BANNER_NEWS, "테스트 제목3", "테스트 내용3"),
        Triple(BANNER_SERVICE, "테스트 제목4", "테스트 내용4"),
        Triple(BANNER_NEWS, "테스트 제목5", "테스트 내용5"),
        Triple(BANNER_SERVICE, "테스트 제목6", "테스트 내용6")
    )
    AppBar.HomeBanner(data = dummyData)
}
