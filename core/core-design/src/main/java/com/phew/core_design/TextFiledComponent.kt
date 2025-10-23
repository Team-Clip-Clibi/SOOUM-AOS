package com.phew.core_design

import androidx.annotation.DrawableRes
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

object TextFiledComponent {
    @Composable
    fun NoIcon(
        value: String,
        onValueChange: (String) -> Unit,
        placeHolder: String = "",
        useHelper: Boolean = false,
        helperText: String = "",
        helperTextColor: Color = NeutralColor.BLACK,
        showError: Boolean = false,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeHolder,
                        style = TextComponent.SUBTITLE_1_M_16,
                        color = NeutralColor.GRAY_500
                    )
                },
                textStyle = TextComponent.SUBTITLE_1_M_16.copy(color = NeutralColor.BLACK),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(10.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = NeutralColor.GRAY_100,
                    unfocusedContainerColor = NeutralColor.GRAY_100,
                    disabledContainerColor = NeutralColor.GRAY_100,
                    errorContainerColor = NeutralColor.GRAY_100,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    cursorColor = NeutralColor.BLACK
                ),
            )

            if (useHelper) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        if (showError) 6.dp else 0.dp,
                        Alignment.Start
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showError) {
                        Icon(
                            painter = painterResource(R.drawable.ic_error_stoke),
                            contentDescription = "Error $helperText",
                            tint = Danger.M_RED
                        )
                    }
                    Text(
                        text = helperText,
                        style = TextComponent.CAPTION_2_M_12,
                        color = if (showError) Danger.M_RED else helperTextColor
                    )
                }
            }
        }
    }

    @Composable
    fun RightIcon(
        @DrawableRes rightImage: Int = R.drawable.ic_delete,
        rightImageClick: () -> Unit,
        value: String,
        onValueChange: (String) -> Unit,
        placeHolder: String,
        helperUse: Boolean,
        helperText: String = "",
        helperTextColor: Color = NeutralColor.GRAY_500,
        showError: Boolean = false,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(color = NeutralColor.GRAY_100, shape = RoundedCornerShape(10.dp))
                    .padding(start = 24.dp, end = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            text = placeHolder,
                            style = TextComponent.SUBTITLE_1_M_16,
                            color = NeutralColor.GRAY_500
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        cursorColor = NeutralColor.BLACK
                    ),
                )
                Icon(
                    painter = painterResource(rightImage),
                    contentDescription = "remove all text",
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                        .clickable {
                            rightImageClick()
                        }
                )
            }
            if (helperUse) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        if (showError) 6.dp else 0.dp,
                        Alignment.Start
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showError) {
                        Icon(
                            painter = painterResource(R.drawable.ic_error_stoke),
                            contentDescription = "Error $helperText",
                            tint = Danger.M_RED
                        )
                    }
                    Text(
                        text = helperText,
                        style = TextComponent.CAPTION_2_M_12,
                        color = if (showError) Danger.M_RED else helperTextColor
                    )
                }
            }
        }
    }

    /**
     * TODO 호출 시 글자 수 제한은 VIEW 에서
     */
    @Composable
    fun TextArea(
        value: String,
        maxValueLength: Int = 0,
        placeHolder: String = "",
        onValueChange: (String) -> Unit,
    ) {
        val focusRequester = remember { FocusRequester() }
        val interaction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(199.dp)
                .background(color = NeutralColor.GRAY_100, shape = RoundedCornerShape(10.dp))
                .padding(start = 24.dp, top = 15.dp, end = 24.dp, bottom = 15.dp)
                .clickable(
                    interactionSource = interaction,
                    indication = null
                ) { focusRequester.requestFocus() }
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeHolder,
                        style = TextComponent.SUBTITLE_1_M_16,
                        color = NeutralColor.GRAY_500
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    cursorColor = NeutralColor.BLACK
                ),
            )
            Text(
                text = "${value.length}/${maxValueLength}",
                style = TextComponent.CAPTION_2_M_12,
                color = NeutralColor.GRAY_400,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(top = 6.dp)
            )
        }
    }
}

@Composable
@Preview
private fun Preview() {
    TextFiledComponent.TextArea(
        value = "Compose Test",
        onValueChange = {

        },
        placeHolder = "hello compose",
        maxValueLength = 1000
    )
}