package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.spacing

/**
 * Shared stepper card for configuring mission repetitions or difficulty steps.
 */
@Composable
internal fun MissionStepperGroup(
    title: String,
    valueText: String,
    helperText: String,
    canDecrement: Boolean,
    canIncrement: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onReset: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ReMindSettingsGroup(
        modifier = modifier,
        title = title,
        headerTrailingContent = onReset?.let { reset ->
            {
                IconButton(
                    onClick = reset,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = ReMindIcons.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(MaterialTheme.spacing.mediumLarge)
                    )
                }
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.small
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onDecrement,
                enabled = canDecrement,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = ReMindIcons.KeyboardArrowLeft,
                    contentDescription = null,
                    modifier = Modifier.size(MaterialTheme.spacing.large)
                )
            }

            Text(
                text = valueText,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            FilledTonalIconButton(
                onClick = onIncrement,
                enabled = canIncrement,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = ReMindIcons.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(MaterialTheme.spacing.large)
                )
            }
        }

        Text(
            text = helperText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start = MaterialTheme.spacing.medium,
                end = MaterialTheme.spacing.medium,
                bottom = MaterialTheme.spacing.medium
            )
        )
    }
}

/**
 * Shared single-choice radio option row inside a [ReMindSettingsGroup].
 */
@Composable
internal fun MissionRadioOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        leadingContent = {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            style = if (selected) {
                LiteverTokens.typography.bodyLargeEmphasized
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
