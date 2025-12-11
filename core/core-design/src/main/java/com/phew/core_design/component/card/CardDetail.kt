package com.phew.core_design.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import com.phew.core_design.typography.FontType
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.R
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.core.net.toUri

@Composable
fun CardDetail(
    modifier: Modifier = Modifier,
    isDeleted: Boolean = false,
    previousCommentThumbnailUri: String? = null,
    backgroundImageUrl : Uri? =null,
    cardContent: String,
    cardThumbnailUri: String,
    cardTags: List<String>,
    fontType: FontType? = null,
    header: @Composable () -> Unit,
    bottom: @Composable () -> Unit?,
    onPreviousCardClick: () -> Unit = { },
    onTagClick: (String) -> Unit = { }
) {
    Column(
        modifier = modifier
            .background(NeutralColor.WHITE)
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 20.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        header()

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isDeleted) {
                CardView(
                    data = BaseCardData.Deleted(
                        reason = stringResource(R.string.card_deleted)
                    ),
                )
            } else {
                CardView(
                    modifier = modifier.padding(vertical = 2.dp),
                    data = BaseCardData.Reply(
                        previousCommentThumbnailUri = previousCommentThumbnailUri,
                        content = cardContent,
                        tags = cardTags,
                        hasPreviousCommentThumbnail = !previousCommentThumbnailUri.isNullOrEmpty(),
                        thumbnailUri = cardThumbnailUri,
                        backgroundImage = backgroundImageUrl,
                        fontType = fontType,
                        onTagClick = onTagClick
                    ),
                    onPreviousCardClick = onPreviousCardClick
                )
            }
        }

        bottom()
    }
}