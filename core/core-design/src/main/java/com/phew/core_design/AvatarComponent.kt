package com.phew.core_design

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

object AvatarComponent {
    @Composable
    fun LargeAvatar(
        url: Uri = Uri.EMPTY,
        onClick : () -> Unit
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(120.dp)
                .clickable { onClick() }
        ) {
            AsyncImage(
                model = if (url == Uri.EMPTY) R.drawable.ic_profile else url,
                contentDescription = "profile image",
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
            Row(
                modifier = Modifier
                    .size(32.dp)
                    .border(
                        width = 1.dp,
                        color = NeutralColor.GRAY_200,
                        shape = RoundedCornerShape(size = 100.dp)
                    )
                    .background(
                        color = NeutralColor.WHITE,
                        shape = RoundedCornerShape(size = 100.dp)
                    )
                    .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 4.dp)
                    .align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_camera_filled),
                    contentDescription = "select profile image",
                    tint = NeutralColor.GRAY_400,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    @Composable
    fun MediumAvatar(
        url: String = "",
    ) {
        Column(
            modifier = Modifier
                .width(60.dp)
                .height(60.dp)
        ) {
            AsyncImage(
                model = if (url.trim().isEmpty()) R.drawable.ic_profile else url,
                contentDescription = "profile image",
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    @Composable
    fun SmallAvatar(
        url: String = "",
    ) {
        Column(
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
        ) {
            AsyncImage(
                model = if (url.trim().isEmpty()) R.drawable.ic_profile else url,
                contentDescription = "profile image",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
@Preview
private fun Preview() {
    AvatarComponent.LargeAvatar(onClick = {})
}