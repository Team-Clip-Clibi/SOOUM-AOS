package com.phew.core_design

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.res.painterResource

object CardViewComponent {

    @Composable
    fun TemporaryCard(
        limitedTime: String,
        location: String,
        writeTime: String,
        commentValue: String,
        likeValue: String,
        uri: Uri,
        content: String,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(206.dp)
                .border(
                    width = 1.dp,
                    color = NeutralColor.GRAY_100,
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .clip(RoundedCornerShape(size = 16.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "SOOUM FEED $content",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 32.dp, end = 32.dp, bottom = 32.dp)
                        .height(82.dp)
                        .background(
                            color = OpacityColor.blackSmallColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = content,
                        style = TextComponent.BODY_1_M_14,
                        color = NeutralColor.WHITE,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(color = NeutralColor.WHITE)
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_bomb),
                    contentDescription = "Time Limit card : $limitedTime",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = limitedTime,
                    style = TextComponent.CAPTION_2_M_12,
                    color = Primary.DARK,
                    modifier = Modifier.padding(start = 2.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(R.drawable.ic_spot),
                    contentDescription = "Time Limit card : $limitedTime",
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(R.drawable.ic_location),
                    modifier = Modifier.size(14.dp),
                    contentDescription = "location $location",
                )
                Text(
                    text = location,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(start = 2.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(R.drawable.ic_spot),
                    contentDescription = "Time Limit card : $limitedTime",
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = writeTime,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = painterResource(R.drawable.ic_message_circle),
                    contentDescription = "comment $commentValue",
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = commentValue,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                Image(
                    painter = painterResource(R.drawable.ic_heart),
                    contentDescription = "like $likeValue",
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = likeValue,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }

    @Composable
    fun FeedCardView(
        location: String,
        writeTime: String,
        commentValue: String,
        likeValue: String,
        uri: Uri,
        content: String,
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(206.dp)
                .border(
                    width = 1.dp,
                    color = NeutralColor.GRAY_100,
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .clip(RoundedCornerShape(size = 16.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "SOOUM FEED $content",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 32.dp, end = 32.dp, bottom = 32.dp)
                        .height(82.dp)
                        .background(
                            color = OpacityColor.blackSmallColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = content,
                        style = TextComponent.BODY_1_M_14,
                        color = NeutralColor.WHITE,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(color = NeutralColor.WHITE)
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_location),
                    modifier = Modifier.size(14.dp),
                    contentDescription = "location $location",
                )
                Text(
                    text = location,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(start = 2.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(R.drawable.ic_spot),
                    contentDescription = "Time Limit card : $location",
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = writeTime,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = painterResource(R.drawable.ic_heart),
                    contentDescription = "like $likeValue",
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = likeValue,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                Image(
                    painter = painterResource(R.drawable.ic_message_circle),
                    contentDescription = "comment $commentValue",
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = commentValue,
                    style = TextComponent.CAPTION_2_M_12,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    CardViewComponent.FeedCardView(
        location = "600m",
        writeTime = "방금전",
        commentValue = "1",
        likeValue = "1",
        uri = Uri.EMPTY,
        content = "hello world"
    )
}