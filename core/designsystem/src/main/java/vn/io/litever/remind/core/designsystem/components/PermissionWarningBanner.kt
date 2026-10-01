package vn.io.litever.remind.core.designsystem.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

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
        LiteverTheme.colors.errorContainer
    } else {
        LiteverTheme.colors.warningContainer
    }

    val contentColor = if (isCritical) {
        LiteverTheme.colors.onErrorContainer
    } else {
        LiteverTheme.colors.onWarningContainer
    }

    val icon = if (isCritical) {
        ReMindIcons.ErrorOutline
    } else {
        ReMindIcons.Warning
    }

    val buttonSemantic = if (isCritical) {
        LvSemantic.Destructive
    } else {
        LvSemantic.Warning
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        contentColor = contentColor,
        shape = LiteverTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(LiteverTheme.spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))
                Text(
                    text = title,
                    style = LiteverTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))

            Text(
                text = message,
                style = LiteverTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.smallMedium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDismiss != null) {
                    LvButton(
                        onClick = onDismiss,
                        type = LvButtonType.Outlined,
                        semantic = buttonSemantic
                    ) {
                        Text(dismissLabel)
                    }
                    Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))
                }
                LvButton(
                    onClick = onClick,
                    semantic = buttonSemantic
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
            modifier = Modifier.padding(LiteverTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
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
