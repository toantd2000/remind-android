package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverListItem
import vn.io.litever.designsystem.components.LiteverModalBottomSheet
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.features.reminders.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderActionBottomSheet(
    reminder: Reminder,
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onToggleCompletedClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiteverModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Edit
            LiteverListItem(
                headlineContent = { Text(stringResource(R.string.action_edit)) },
                leadingContent = {
                    Icon(
                        imageVector = ReMindIcons.Edit,
                        contentDescription = null,
                        tint = LiteverTheme.colors.onSurface
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        onEditClick()
                    }
            )

            // Duplicate
            LiteverListItem(
                headlineContent = { Text(stringResource(R.string.action_duplicate)) },
                leadingContent = {
                    Icon(
                        imageVector = ReMindIcons.ContentCopy,
                        contentDescription = null,
                        tint = LiteverTheme.colors.onSurface
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        onDuplicateClick()
                    }
            )

            // Mark as completed / active
            val completionText = if (reminder.isCompleted) {
                stringResource(R.string.action_mark_active)
            } else {
                stringResource(R.string.action_mark_completed)
            }
            val completionIcon = if (reminder.isCompleted) {
                ReMindIcons.NotificationsActive
            } else {
                ReMindIcons.CheckCircle
            }
            LiteverListItem(
                headlineContent = { Text(completionText) },
                leadingContent = {
                    Icon(
                        imageVector = completionIcon,
                        contentDescription = null,
                        tint = LiteverTheme.colors.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        onToggleCompletedClick()
                    }
            )

            // Delete
            LiteverListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = LiteverTheme.colors.error
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = ReMindIcons.Delete,
                        contentDescription = null,
                        tint = LiteverTheme.colors.error
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        onDeleteClick()
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
