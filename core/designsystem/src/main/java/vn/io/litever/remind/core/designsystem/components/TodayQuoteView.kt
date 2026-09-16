package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.AdConfig
import vn.io.litever.remind.core.model.TodayBriefing

@Composable
fun TodayQuoteView(
    todayBriefing: TodayBriefing?,
    modifier: Modifier = Modifier
) {
    if (todayBriefing == null) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = LiteverTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Left Accent Bar (Bookmark effect for the entire block)
            Box(
                modifier = Modifier
                    .matchParentSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(LiteverTheme.spacing.extraSmall)
                        .background(
                            color = LiteverTheme.colors.onTertiaryContainer
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = LiteverTheme.spacing.extraSmall)
                    .padding(vertical = LiteverTheme.spacing.small)
            ) {
                todayBriefing.messages.forEachIndexed { index, msg ->
                    Text(
                        text = msg,
                        modifier = Modifier
                            .padding(
                                start = LiteverTheme.spacing.smallMedium,
                                end = LiteverTheme.spacing.medium
                            )
                            .padding(vertical = LiteverTheme.spacing.small),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic,
                            color = LiteverTheme.colors.onTertiaryContainer
                        )
                    )

                    if (index < todayBriefing.messages.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = LiteverTheme.spacing.medium),
                            thickness = 0.5.dp,
                            color = LiteverTheme.colors.tertiaryContainer.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodayQuoteViewPreview() {
    val mockTodayBriefing = TodayBriefing(
        messages = listOf(
            "Đừng quên mang theo tài liệu họp lúc 9h sáng nhé!",
            "Uống thuốc sau khi ăn sáng."
        ),
        adConfig = AdConfig(enableAds = false)
    )
    ReMindTheme {
        Box(modifier = Modifier.padding(LiteverTheme.spacing.medium)) {
            TodayQuoteView(todayBriefing = mockTodayBriefing)
        }
    }
}
