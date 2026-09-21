package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.features.mission.R

/**
 * Banner hướng dẫn ngắn gọn cho các màn hình nhiệm vụ theo chuẩn phong cách Stitch.
 * Bao gồm icon đại diện bên trái, cùng dòng chữ yêu cầu rõ ràng, nổi bật.
 */
@Composable
fun MissionInstructionBanner(
    icon: ImageVector,
    requirementText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = LiteverTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = LiteverTheme.colors.tertiaryContainer
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = LiteverTheme.spacing.medium,
                    vertical = LiteverTheme.spacing.smallMedium
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.smallMedium)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LiteverTheme.colors.onTertiaryContainer,
                modifier = Modifier.size(20.dp)
            )

            val annotatedText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = LiteverTheme.colors.onTertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(stringResource(R.string.mission_requirement_prefix))
                    append(" ")
                }
                withStyle(
                    style = SpanStyle(
                        color = LiteverTheme.colors.onSurface
                    )
                ) {
                    append(requirementText)
                }
            }

            Text(
                text = annotatedText,
                style = LiteverTheme.typography.bodySmall,
                lineHeight = LiteverTheme.typography.bodySmall.lineHeight
            )
        }
    }
}
