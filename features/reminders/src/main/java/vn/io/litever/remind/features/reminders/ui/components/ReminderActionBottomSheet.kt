package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.features.reminders.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderActionBottomSheet(
    reminder: Reminder,
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onToggleEnabledClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Turn off / Turn on
            val toggleText = if (reminder.isEnabled) {
                stringResource(R.string.action_turn_off)
            } else {
                stringResource(R.string.action_turn_on)
            }
            val toggleIcon = if (reminder.isEnabled) {
                ReMindIcons.NotificationsPaused
            } else {
                ReMindIcons.NotificationsActive
            }
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = toggleIcon,
                        contentDescription = null,
                        tint = if (reminder.isEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        onToggleEnabledClick()
                    }
            ) {
                Text(toggleText)
            }

            // Edit
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = ReMindIcons.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        onEditClick()
                    }
            ) {
                Text(stringResource(R.string.action_edit))
            }

            // Duplicate
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = ReMindIcons.ContentCopy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        onDuplicateClick()
                    }
            ) {
                Text(stringResource(R.string.action_duplicate))
            }

            // Delete
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = ReMindIcons.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        onDeleteClick()
                    }
            ) {
                Text(
                    text = stringResource(R.string.action_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
