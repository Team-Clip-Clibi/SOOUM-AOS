package com.phew.presentation.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.phew.core_common.TimeUtils
import com.phew.core_design.AppBar.TextButtonAppBar
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import com.phew.presentation.detail.R as DetailR

@Composable
internal fun CardDetailTopBar(
    remainingTimeMillis: Long,
    onBackPressed: () -> Unit,
    onMoreClick: () -> Unit,
    title: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeutralColor.WHITE)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButtonAppBar(
            startImage = R.drawable.ic_left,
            endImage = R.drawable.ic_more_stroke_circle,
            appBarText = title ?: stringResource(DetailR.string.card_title_comment),
            startClick = onBackPressed,
            endClick = onMoreClick
        )
        val timer = TimeUtils.formatMillisToTimer(remainingTimeMillis)
        if (timer.isNotBlank() && remainingTimeMillis.toString().trim() != "0") {
            Box(
                modifier = Modifier
                    .width(53.dp)
                    .height(23.dp)
                    .background(NeutralColor.WHITE)
                    .border(1.dp, NeutralColor.GRAY_200, RoundedCornerShape(100.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timer,
                    color = Primary.DARK,
                    style = TextComponent.CAPTION_3_M_10,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
