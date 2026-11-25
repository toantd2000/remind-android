package vn.io.litever.remind.features.mission.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.assets.LiteverMascotState
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.designsystem.components.ReMindMascotEmptyState
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.semanticColors
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.features.mission.R

@Composable
fun MissionCompleteContent(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.mission_complete_title),
    subtitle: String = stringResource(R.string.mission_complete_subtitle),
    completedStep: Int? = null,
    totalSteps: Int? = null,
) {
    val isInspectionMode = LocalInspectionMode.current
    var visible by remember { mutableStateOf(isInspectionMode) }
    LaunchedEffect(Unit) {
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.86f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "missionCompleteScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 320),
        label = "missionCompleteAlpha"
    )

    val glowColor = MaterialTheme.semanticColors.success
    val showStepBadge = completedStep != null && totalSteps != null && totalSteps > 1

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.16f * alpha),
                            Color.Transparent
                        ),
                        center = Offset(x = size.width / 2f, y = size.height * 0.44f),
                        radius = size.minDimension * 0.65f
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        ReMindMascotEmptyState(
            mascot = LiteverMascotState.SUCCESS,
            title = title,
            description = subtitle,
            badge = if (showStepBadge) {
                {
                    Surface(
                        color = MaterialTheme.semanticColors.success.copy(alpha = 0.14f),
                        contentColor = MaterialTheme.semanticColors.success,
                        shape = CircleShape
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(
                                horizontal = MaterialTheme.spacing.smallMedium,
                                vertical = MaterialTheme.spacing.extraSmall + 2.dp
                            )
                        ) {
                            Icon(
                                imageVector = ReMindIcons.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                            Text(
                                text = "$completedStep / $totalSteps",
                                style = LiteverTokens.typography.labelLargeEmphasized.copy(
                                    fontFeatureSettings = "tnum"
                                )
                            )
                        }
                    }
                }
            } else {
                null
            },
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
        )
    }
}

@Preview(showBackground = true, name = "Mission Complete - Final")
@Composable
private fun MissionCompleteContentPreview() {
    ReMindTheme {
        MissionCompleteContent()
    }
}

@Preview(showBackground = true, name = "Mission Complete - Next Step", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MissionCompleteNextStepPreview() {
    ReMindTheme(darkTheme = true) {
        MissionCompleteContent(
            subtitle = "Next task starting...",
            completedStep = 2,
            totalSteps = 4
        )
    }
}
