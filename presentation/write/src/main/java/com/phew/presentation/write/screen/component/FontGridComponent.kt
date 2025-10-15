package com.phew.presentation.write.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
    modifier: Modifier = Modifier
) {

    // TODO Lazy 보다는 일반 Vertical로 고민 필요
    Column(modifier = modifier.padding(16.dp)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(fonts) { font ->
                val isSelected = font.name == selectedFont
                if (isSelected){
                    SelectedSecondary(
                        buttonText = font.name,
                        onClick = { font.previewTypeface?.let { onFontSelected(it) } },
                        isEnable = true
                    )
                } else {
                    DisabledSecondary(
                        buttonText = font.name,
                        onClick = { font.previewTypeface?.let { onFontSelected(it) } },
                        isEnable = false
                    )
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
        FontItem("프리텐다드", pretendard),
        FontItem("리디바탕", ridibatang),
        FontItem("윤우체", yoon),
        FontItem("꼭꼭체", kkokko)
    )

    FontSelectorGrid(
        fonts = fontList,
        selectedFont = fontList.first().name,
        onFontSelected = {  }
    )
}