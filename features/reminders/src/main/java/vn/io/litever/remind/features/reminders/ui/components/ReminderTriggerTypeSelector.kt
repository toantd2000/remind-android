package vn.io.litever.remind.features.reminders.ui.components
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.features.reminders.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTriggerTypeSelector(
    selectedTriggerType: ReminderTriggerType,
    onTriggerTypeSelected: (ReminderTriggerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTabIndex = ReminderTriggerType.entries.indexOf(selectedTriggerType).coerceAtLeast(0)

    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier.fillMaxWidth(),
    ) {
        ReminderTriggerType.entries.forEach { triggerType ->
            val (labelRes: Int, icon: ImageVector) = when (triggerType) {
                ReminderTriggerType.FIXED_TIME -> Pair(R.string.trigger_fixed_time, ReMindIcons.AccessTime)
                ReminderTriggerType.INTERVAL -> Pair(R.string.trigger_interval, ReMindIcons.Timer)
            }

            Tab(
                selected = triggerType == selectedTriggerType,
                onClick = { onTriggerTypeSelected(triggerType) },
                text = {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.tiny)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(labelRes),
                            fontWeight = if (selectedTriggerType == triggerType) FontWeight.Bold else FontWeight.SemiBold,
                        )
                    }
                },
            )
        }
    }
}