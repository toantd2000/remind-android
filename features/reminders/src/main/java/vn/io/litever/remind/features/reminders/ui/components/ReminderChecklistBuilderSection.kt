package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverIconButton
import vn.io.litever.designsystem.components.LiteverOutlinedCard
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

    val handleAddItem = {
        if (newItemText.isNotBlank()) {
            onAddItem(newItemText.trim())
            newItemText = ""
        }
    }

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
            // Section Header
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
                        color = LiteverTheme.colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Checklist Items List
            if (checklist.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_checklist_items),
                    style = LiteverTheme.typography.bodySmall,
                    color = LiteverTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    checklist.forEachIndexed { index, item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(LiteverTheme.shapes.medium),
                            color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.35f),
                            shape = LiteverTheme.shapes.medium
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                // Drag Handle at Start
                                Icon(
                                    imageVector = Icons.Rounded.DragHandle,
                                    contentDescription = stringResource(R.string.reorder_handle),
                                    tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .padding(start = 4.dp, end = 8.dp)
                                        .size(20.dp)
                                )

                                // Flat modern input
                                BasicTextField(
                                    value = item.text,
                                    onValueChange = { onUpdateItemText(index, it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 6.dp),
                                    textStyle = LiteverTheme.typography.bodyMedium.copy(
                                        color = LiteverTheme.colors.onSurface
                                    ),
                                    cursorBrush = SolidColor(LiteverTheme.colors.primary),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (item.text.isEmpty()) {
                                                Text(
                                                    text = stringResource(R.string.checklist_item_placeholder),
                                                    style = LiteverTheme.typography.bodyMedium,
                                                    color = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )

                                // Delete button at End
                                LiteverIconButton(
                                    onClick = { onDeleteItem(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Delete",
                                        tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Add New Item Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiteverTheme.shapes.medium),
                color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.2f),
                shape = LiteverTheme.shapes.medium
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    BasicTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        textStyle = LiteverTheme.typography.bodyMedium.copy(
                            color = LiteverTheme.colors.onSurface
                        ),
                        cursorBrush = SolidColor(LiteverTheme.colors.primary),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { handleAddItem() }),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (newItemText.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.checklist_item_placeholder),
                                        style = LiteverTheme.typography.bodyMedium,
                                        color = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    LiteverIconButton(
                        onClick = handleAddItem,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.add_checklist_item),
                            tint = if (newItemText.isNotBlank()) LiteverTheme.colors.primary else LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
