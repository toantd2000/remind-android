package vn.io.litever.remind.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.core.designsystem.theme.semanticColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.errorColors
import vn.io.litever.remind.core.designsystem.theme.warningColors

/**
 * Common Permission Warning Banner for Alarms and Reminders features.
 *
 * Displays:
 * - Error state (Critical): when core permissions (Notification/Exact Alarm) are missing.
 * - Warning state (Non-Critical): when optional permissions (Battery Optimization, Overlay, etc.) are missing,
 *   offering a "Remind me later" dismiss button.
 */
@Composable
fun PermissionWarningBanner(
    title: String,
    message: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String = stringResource(R.string.permission_warning_banner_action),
    isCritical: Boolean = true,
    dismissLabel: String = stringResource(R.string.permission_warning_banner_remind_later),
    onDismiss: (() -> Unit)? = null
) {
    val containerColor = if (isCritical) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.semanticColors.warningContainer
    }

    val contentColor = if (isCritical) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.semanticColors.onWarningContainer
    }

    val icon = if (isCritical) {
        ReMindIcons.ErrorOutline
    } else {
        ReMindIcons.Warning
    }

    val buttonColors = if (isCritical) {
        ButtonDefaults.errorColors()
    } else {
        ButtonDefaults.warningColors()
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDismiss != null) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(dismissLabel)
                    }
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                }
                Button(
                    onClick = onClick,
                    colors = buttonColors
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Permission Warning - Critical (Error)")
@Composable
private fun PermissionWarningBannerErrorPreview() {
    ReMindTheme {
        PermissionWarningBanner(
            title = "Missing Permissions",
            message = "Notifications and Exact Alarm permissions are required for the app to function properly.",
            onClick = {},
            isCritical = true
        )
    }
}

@Preview(showBackground = true, name = "Permission Warning - Non-critical (Warning)")
@Composable
private fun PermissionWarningBannerWarningPreview() {
    ReMindTheme {
        PermissionWarningBanner(
            title = "Permission Recommendation",
            message = "Optional permissions ensure reliable operation on lock screen and during battery saving.",
            onClick = {},
            isCritical = false,
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Permission Warning - Dark Mode")
@Composable
private fun PermissionWarningBannerDarkPreview() {
    ReMindTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            PermissionWarningBanner(
                title = "Missing Permissions",
                message = "Notifications are required.",
                onClick = {},
                isCritical = true
            )
            PermissionWarningBanner(
                title = "Permission Recommendation",
                message = "Battery optimization helps notifications trigger reliably.",
                onClick = {},
                isCritical = false,
                onDismiss = {}
            )
        }
    }
}
