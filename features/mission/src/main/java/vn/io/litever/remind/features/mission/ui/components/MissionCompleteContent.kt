package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
        title = title,
        description = subtitle,
        type = FeedbackStateType.SUCCESS,
        modifier = modifier.fillMaxSize(),
        titleStyle = LiteverTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
        ),
        descriptionStyle = LiteverTheme.typography.bodyLarge.copy()
    )
}

@Preview(showBackground = true)
@Composable
fun MissionCompleteContentPreview() {
    ReMindTheme {
        MissionCompleteContent()
    }
}