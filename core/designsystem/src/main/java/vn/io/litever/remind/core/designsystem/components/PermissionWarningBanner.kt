package vn.io.litever.remind.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.semanticColors
import vn.io.litever.remind.core.designsystem.theme.spacing

/**
 * Animated shared section wrapper for [PermissionWarningBanner] across Alarms and Reminders list screens.
 */
@Composable
fun PermissionWarningBannerSection(
    visible: Boolean,
    isCritical: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    title: String? = null,
    message: String? = null,
    actionLabel: String = stringResource(R.string.permission_warning_banner_action),
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        PermissionWarningBanner(
            title = title ?: stringResource(
                if (isCritical) {
                    R.string.permission_warning_banner_title
                } else {
                    R.string.permission_warning_banner_non_critical_title
                }
            ),
            message = message ?: stringResource(
                if (isCritical) {
                    R.string.permission_warning_banner_desc
                } else {
                    R.string.permission_warning_banner_non_critical_desc
                }
            ),
            onClick = onClick,
            modifier = modifier,
            actionLabel = actionLabel,
            isCritical = isCritical,
            onDismiss = if (isCritical) null else onDismiss,
        )
    }
}

/**
 * Compact Inline Permission Warning Banner shared by Alarms and Reminders list screens.
 *
 * Displays:
 * - Critical (Error) state: when required permissions are missing.
 * - Non-Critical (Warning) state: when recommended permissions are missing, with a compact dismiss button.
 */
@Composable
fun PermissionWarningBanner(
    title: String,
    message: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String = stringResource(R.string.permission_warning_banner_action),
    isCritical: Boolean = true,
    onDismiss: (() -> Unit)? = null,
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
    val accentColor = if (isCritical) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.semanticColors.warning
    }
    val onAccentColor = if (isCritical) {
        MaterialTheme.colorScheme.onError
    } else {
        MaterialTheme.semanticColors.onWarning
    }
    val icon = if (isCritical) {
        ReMindIcons.Error
    } else {
        ReMindIcons.Warning
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.smallMedium,
                    vertical = MaterialTheme.spacing.smallMedium
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.smallMedium))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = LiteverTokens.typography.titleSmallEmphasized,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

            Surface(
                onClick = onClick,
                shape = CircleShape,
                color = accentColor,
                contentColor = onAccentColor
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.smallMedium,
                        vertical = 6.dp
                    )
                )
            }

            if (onDismiss != null) {
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = ReMindIcons.Close,
                        contentDescription = stringResource(R.string.permission_warning_banner_dismiss),
                        tint = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
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
            title = "Missing permissions",
            message = "Tap to enable reliable alerts",
            onClick = {},
            isCritical = true,
            modifier = Modifier.padding(MaterialTheme.spacing.medium)
        )
    }
}

@Preview(showBackground = true, name = "Permission Warning - Non-critical (Warning)")
@Composable
private fun PermissionWarningBannerWarningPreview() {
    ReMindTheme {
        PermissionWarningBanner(
            title = "Optimize reliability",
            message = "Recommended permissions missing",
            onClick = {},
            isCritical = false,
            onDismiss = {},
            modifier = Modifier.padding(MaterialTheme.spacing.medium)
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
                title = "Missing permissions",
                message = "Tap to enable reliable alerts",
                onClick = {},
                isCritical = true
            )
            PermissionWarningBanner(
                title = "Optimize reliability",
                message = "Recommended permissions missing",
                onClick = {},
                isCritical = false,
                onDismiss = {}
            )
        }
    }
}
