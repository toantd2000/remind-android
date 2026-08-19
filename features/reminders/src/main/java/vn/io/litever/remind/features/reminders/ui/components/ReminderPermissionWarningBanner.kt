package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderPermissionWarningBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    customMessage: String? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = LiteverTheme.colors.errorContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = LiteverTheme.colors.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.reminder_permission_warning_banner_title),
                    style = LiteverTheme.typography.titleSmall,
                    color = LiteverTheme.colors.error,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = customMessage ?: stringResource(R.string.reminder_permission_warning_banner_desc),
                style = LiteverTheme.typography.bodySmall,
                color = LiteverTheme.colors.onErrorContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            LiteverButton(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LiteverTheme.colors.error,
                    contentColor = LiteverTheme.colors.onError
                )
            ) {
                Text(stringResource(R.string.permission_warning_banner_action))
            }
        }
    }
}
