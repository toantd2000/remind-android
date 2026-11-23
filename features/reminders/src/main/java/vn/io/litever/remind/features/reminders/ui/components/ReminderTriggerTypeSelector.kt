package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderTriggerTypeSelector(
    selectedTriggerType: ReminderTriggerType,
    onTriggerTypeSelected: (ReminderTriggerType) -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReminderTriggerType.entries.forEach { triggerType ->
            val (labelRes: Int, icon: ImageVector) = when (triggerType) {
                ReminderTriggerType.FIXED_TIME -> Pair(R.string.trigger_fixed_time, ReMindIcons.AccessTime)
                ReminderTriggerType.INTERVAL -> Pair(R.string.trigger_interval, ReMindIcons.Timer)
            }
            val isSelected = triggerType == selectedTriggerType

            FilterChip(
                selected = isSelected,
                onClick = { onTriggerTypeSelected(triggerType) },
                label = {
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = MaterialTheme.shapes.medium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderTriggerTypeSelectorFixedTimePreview() {
    ReMindTheme {
        ReminderTriggerTypeSelector(
            selectedTriggerType = ReminderTriggerType.FIXED_TIME,
            onTriggerTypeSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderTriggerTypeSelectorIntervalPreview() {
    ReMindTheme {
        ReminderTriggerTypeSelector(
            selectedTriggerType = ReminderTriggerType.INTERVAL,
            onTriggerTypeSelected = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderTriggerTypeSelectorDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderTriggerTypeSelector(
            selectedTriggerType = ReminderTriggerType.FIXED_TIME,
            onTriggerTypeSelected = {}
        )
    }
}