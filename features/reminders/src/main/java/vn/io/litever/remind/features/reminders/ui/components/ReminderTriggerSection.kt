package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverOutlinedCard
import vn.io.litever.designsystem.components.LiteverSegmentedButton
import vn.io.litever.designsystem.components.LiteverSingleChoiceSegmentedButtonRow
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderTriggerSection(
    selectedTriggerType: ReminderTriggerType,
    onTriggerTypeSelected: (ReminderTriggerType) -> Unit,
    modifier: Modifier = Modifier
) {
    LiteverOutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.section_trigger_type),
                style = LiteverTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            LiteverSingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                ReminderTriggerType.entries.forEachIndexed { index, triggerType ->
                    val (labelRes: Int, icon: ImageVector) = when (triggerType) {
                        ReminderTriggerType.TIME -> R.string.trigger_time to Icons.Rounded.AccessTime
                        ReminderTriggerType.LOCATION -> R.string.trigger_location to Icons.Rounded.LocationOn
                        ReminderTriggerType.WIFI -> R.string.trigger_wifi to Icons.Rounded.Wifi
                    }

                    LiteverSegmentedButton(
                        onClick = { onTriggerTypeSelected(triggerType) },
                        selected = triggerType == selectedTriggerType,
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(labelRes),
                                style = LiteverTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }
        }
    }
}
