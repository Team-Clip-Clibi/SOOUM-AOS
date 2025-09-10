package com.phew.core_design

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign

object AppBar {
    @Composable
    fun HomeAppBar(newAlarm: Boolean, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(color = NeutralColor.WHITE)
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
                painter = if (newAlarm) painterResource(R.drawable.ic_bell_new) else painterResource(
                    R.drawable.ic_bell
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
        @DrawableRes endImage: Int = R.drawable.ic_home,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(NeutralColor.WHITE)
                .padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(image),
                    contentDescription = "left icon",
                    modifier = Modifier.size(24.dp)
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
}

@Composable
@Preview
private fun Preview() {
    AppBar.IconLeftAppBar(onClick = {})
}