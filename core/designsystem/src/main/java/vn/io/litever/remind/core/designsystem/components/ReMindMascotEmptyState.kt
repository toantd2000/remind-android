package vn.io.litever.remind.core.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.assets.LiteverMascotState
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing

/**
 * Shared Mascot State component for empty states, completion states, and transition states across ReMind.
 * Keeps copy concise and user-focused without redundant brand text logos.
 */
@Composable
fun ReMindMascotEmptyState(
    mascot: LiteverMascotState,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionIcon: ImageVector = ReMindIcons.Add,
    badge: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.large, vertical = MaterialTheme.spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = mascot.drawableRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(164.dp)
        )

        if (badge != null) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            badge()
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))
        } else {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        }

        Text(
            text = title,
            style = LiteverTokens.typography.titleLargeEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp)
        )

        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            Button(
                onClick = onActionClick,
                shape = CircleShape,
                contentPadding = PaddingValues(
                    horizontal = MaterialTheme.spacing.large,
                    vertical = MaterialTheme.spacing.smallMedium
                )
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = actionLabel,
                    style = LiteverTokens.typography.labelLargeEmphasized
                )
            }
        }
    }
}

@Preview(name = "Mascot Empty State - Light", showBackground = true, device = Devices.PIXEL_7)
@Composable
private fun ReMindMascotEmptyStatePreview() {
    ReMindTheme {
        ReMindMascotEmptyState(
            mascot = LiteverMascotState.IDLE,
            title = "No alarms yet",
            description = "Set your first alarm to wake up on time.",
            actionLabel = "Add alarm",
            onActionClick = {}
        )
    }
}

@Preview(
    name = "Mascot Empty State - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL_7,
    backgroundColor = 0xFF121212L
)
@Composable
private fun ReMindMascotEmptyStateDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReMindMascotEmptyState(
            mascot = LiteverMascotState.EMPTY,
            title = "No reminders yet",
            description = "Add a reminder so nothing slips your mind.",
            actionLabel = "Add reminder",
            onActionClick = {}
        )
    }
}
