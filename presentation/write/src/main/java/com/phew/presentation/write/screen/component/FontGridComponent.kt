package com.phew.presentation.write.screen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_design.MediumButton.DisabledSecondary
import com.phew.core_design.MediumButton.SelectedSecondary
import com.phew.core_design.R
import com.phew.presentation.write.model.FontItem

@Composable
internal fun FontSelectorGrid(
    fonts: List<FontItem>,
    selectedFont: String,
    onFontSelected: (FontFamily) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth().padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        fonts.chunked(2).forEach { rowFonts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowFonts.forEach { font ->
                    val isSelected = font.name == selectedFont
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSelected) {
                            SelectedSecondary(
                                buttonText = font.name,
                                onClick = { font.previewTypeface?.let { onFontSelected(it) } },
                                isEnable = true,
                                fontFamily = font.previewTypeface ?: FontFamily(Font(R.font.black))
                            )
                        } else {
                            DisabledSecondary(
                                buttonText = font.name,
                                onClick = {font.previewTypeface?.let { onFontSelected(it) }},
                                isEnable = true,
                                fontFamily = font.previewTypeface ?: FontFamily(Font(R.font.medium))
                            )
                        }
                    }
                }

                if (rowFonts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FontSelectorGridPreview() {
    val pretendard = FontFamily(Font(R.font.regular))
    val ridibatang = FontFamily(Font(R.font.ridibatang))
    val yoon = FontFamily(Font(R.font.yoon))
    val kkokko = FontFamily(Font(R.font.kkokko))

    val fontList = listOf(
        FontItem("프리텐다드", "PRETENDARD", pretendard),
        FontItem("리디바탕", "RIDI", ridibatang),
        FontItem("윤우체", "YOONWOO", yoon),
        FontItem("꼭꼭체", "KKOOKKKOOK", kkokko)
    )

    FontSelectorGrid(
        fonts = fontList,
        selectedFont = fontList.first().name,
        onFontSelected = {  }
    )
}
