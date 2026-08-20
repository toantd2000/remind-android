package vn.io.litever.remind.features.reminders.ui.components

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverIconButton
import vn.io.litever.designsystem.components.LiteverOutlinedCard
import vn.io.litever.designsystem.components.LiteverTextField
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderChecklistBuilderSection(
    checklist: List<ChecklistItem>,
    onAddItem: (String) -> Unit,
    onUpdateItemText: (index: Int, text: String) -> Unit,
    onToggleItemInitialCheck: (index: Int, isCompleted: Boolean) -> Unit,
    onDeleteItem: (index: Int) -> Unit,
    onMoveItem: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var newItemText by remember { mutableStateOf("") }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Checklist,
                        contentDescription = null,
                        tint = LiteverTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.section_checklist),
                        style = LiteverTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LiteverTheme.colors.onSurface
                    )
                }
                if (checklist.isNotEmpty()) {
                    Text(
                        text = "(${checklist.size})",
                        style = LiteverTheme.typography.labelMedium,
                        color = LiteverTheme.colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Checklist Items
            if (checklist.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_checklist_items),
                    style = LiteverTheme.typography.bodySmall,
                    color = LiteverTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                checklist.forEachIndexed { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = item.isCompleted,
                            onCheckedChange = { isChecked ->
                                onToggleItemInitialCheck(index, isChecked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = LiteverTheme.colors.primary
                            )
                        )

                        LiteverTextField(
                            value = item.text,
                            onValueChange = { onUpdateItemText(index, it) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        // Move Up
                        if (index > 0) {
                            LiteverIconButton(
                                onClick = { onMoveItem(index, index - 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowUpward,
                                    contentDescription = "Move Up",
                                    tint = LiteverTheme.colors.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Move Down
                        if (index < checklist.size - 1) {
                            LiteverIconButton(
                                onClick = { onMoveItem(index, index + 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowDownward,
                                    contentDescription = "Move Down",
                                    tint = LiteverTheme.colors.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Delete
                        LiteverIconButton(
                            onClick = { onDeleteItem(index) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Delete",
                                tint = LiteverTheme.colors.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add New Item Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LiteverTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    placeholder = stringResource(R.string.checklist_item_placeholder),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                LiteverIconButton(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            onAddItem(newItemText.trim())
                            newItemText = ""
                        }
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.add_checklist_item),
                        tint = LiteverTheme.colors.primary
                    )
                }
            }
        }
    }
}
