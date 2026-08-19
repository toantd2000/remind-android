package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverCard
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderRingingChecklist(
    checklist: List<ChecklistItem>,
    onItemToggle: (itemId: Long, isCompleted: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (checklist.isEmpty()) return

    val completedCount = checklist.count { it.isCompleted }
    val totalCount = checklist.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    LiteverCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.8f)
        ),
        shape = LiteverTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Checklist,
                        contentDescription = null,
                        tint = LiteverTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.interactive_checklist_title),
                        style = LiteverTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = LiteverTheme.colors.onSurface
                    )
                }
                Text(
                    text = stringResource(R.string.checklist_progress_format, completedCount, totalCount),
                    style = LiteverTheme.typography.labelMedium,
                    color = LiteverTheme.colors.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.small),
                color = LiteverTheme.colors.primary,
                trackColor = LiteverTheme.colors.primaryContainer.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            checklist.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(LiteverTheme.shapes.small)
                        .clickable { onItemToggle(item.id, !item.isCompleted) }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Checkbox(
                        checked = item.isCompleted,
                        onCheckedChange = { onItemToggle(item.id, it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = LiteverTheme.colors.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.text,
                        style = LiteverTheme.typography.bodyMedium.copy(
                            textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null
                        ),
                        color = if (item.isCompleted) {
                            LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                        } else {
                            LiteverTheme.colors.onSurface
                        }
                    )
                }
            }
        }
    }
}
