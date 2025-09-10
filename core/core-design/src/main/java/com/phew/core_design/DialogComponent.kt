package com.phew.core_design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth

object DialogComponent {
    @Composable
    fun DefaultButtonOne(
        title: String,
        description: String,
        buttonText: String,
        onClick: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        Dialog(
            onDismissRequest = onDismiss
        ) {
            Column(
                modifier = Modifier
                    .width(271.dp)
                    .height(159.dp)
                    .background(color = NeutralColor.WHITE, shape = RoundedCornerShape(20.dp))
                    .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    style = TextComponent.HEAD_3_B_20,
                    color = NeutralColor.BLACK
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = TextComponent.BODY_1_M_14,
                    color = NeutralColor.GRAY_600
                )
                Spacer(modifier = Modifier.height(24.dp))
                MediumButton.NoIconPrimary(
                    buttonText = buttonText,
                    onClick = onClick
                )
            }
        }
    }

    @Composable
    fun NoDescriptionButtonOne(
        title: String,
        buttonText: String,
        onClick: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        Dialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .width(271.dp)
                    .height(132.dp)
                    .background(
                        color = NeutralColor.WHITE,
                        shape = RoundedCornerShape(size = 20.dp)
                    )
                    .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    style = TextComponent.HEAD_3_B_20,
                    color = NeutralColor.BLACK
                )
                Spacer(modifier = Modifier.height(24.dp))
                MediumButton.NoIconPrimary(
                    buttonText = buttonText,
                    onClick = onClick
                )
            }
        }
    }

    @Composable
    fun DefaultButtonTwo(
        title: String,
        description: String,
        buttonTextStart: String,
        buttonTextEnd: String,
        onClick: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        Dialog(
            onDismissRequest = onDismiss
        ) {
            Column(
                modifier = Modifier
                    .width(271.dp)
                    .height(159.dp)
                    .background(color = NeutralColor.WHITE, shape = RoundedCornerShape(20.dp))
                    .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    style = TextComponent.HEAD_3_B_20,
                    color = NeutralColor.BLACK
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = TextComponent.BODY_1_M_14,
                    color = NeutralColor.GRAY_600
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) {
                        MediumButton.NoIconSecondary(
                            buttonText = buttonTextStart,
                            onClick = onDismiss,

                            )
                    }
                    Box(Modifier.weight(1f)) {
                        MediumButton.NoIconPrimary(
                            buttonText = buttonTextEnd,
                            onClick = onClick,
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun NoDescriptionButtonTwo(
        title: String,
        buttonTextStart: String,
        buttonTextEnd: String,
        onClick: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        Dialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .width(271.dp)
                    .height(132.dp)
                    .background(
                        color = NeutralColor.WHITE,
                        shape = RoundedCornerShape(size = 20.dp)
                    )
                    .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    style = TextComponent.HEAD_3_B_20,
                    color = NeutralColor.BLACK
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) {
                        MediumButton.NoIconSecondary(
                            buttonText = buttonTextStart,
                            onClick = onDismiss,

                            )
                    }
                    Box(Modifier.weight(1f)) {
                        MediumButton.NoIconPrimary(
                            buttonText = buttonTextEnd,
                            onClick = onClick,
                        )
                    }
                }
            }
        }
    }

}

@Composable
@Preview
private fun Preview() {
    DialogComponent.NoDescriptionButtonTwo(
        title = "Title",
        onClick = {

        },
        onDismiss = {

        },
        buttonTextStart = "Label",
        buttonTextEnd = "Label",
    )
}