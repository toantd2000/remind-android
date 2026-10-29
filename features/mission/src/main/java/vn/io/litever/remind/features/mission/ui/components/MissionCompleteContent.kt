package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.designsystem.components.FeedbackStateType
import vn.io.litever.designsystem.components.FeedbackStateView
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.mission.R

@Composable
fun MissionCompleteContent(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.mission_complete_title),
    subtitle: String = stringResource(R.string.mission_complete_subtitle)
) {
    FeedbackStateView(
        modifier = modifier.fillMaxSize(),
        title = {
            Text(
                text = title,
                style = LiteverTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        },
        description = {
            Text(
                text = subtitle,
                style = LiteverTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },
        type = FeedbackStateType.SUCCESS
    )
}

@Preview(showBackground = true)
@Composable
fun MissionCompleteContentPreview() {
    ReMindTheme {
        MissionCompleteContent()
    }
}