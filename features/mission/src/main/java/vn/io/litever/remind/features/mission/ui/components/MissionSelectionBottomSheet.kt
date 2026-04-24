package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.designsystem.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionSelectionBottomSheet(
    onDismissRequest: () -> Unit,
    onMissionTypeSelected: (MissionType) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.mission_selection_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(20.dp)
            )
            
            val missionTypes = listOf(
                MissionItem(
                    type = MissionType.TYPING,
                    title = stringResource(R.string.mission_typing),
                    description = stringResource(R.string.mission_typing_desc),
                    icon = Icons.Rounded.Keyboard,
                    isAvailable = true
                ),
                MissionItem(
                    type = MissionType.MATH,
                    title = stringResource(R.string.mission_math),
                    description = stringResource(R.string.mission_math_desc),
                    icon = Icons.Rounded.Calculate,
                    isAvailable = false
                ),
                MissionItem(
                    type = MissionType.SHAKE,
                    title = stringResource(R.string.mission_shake),
                    description = "Shake your phone to wake up",
                    icon = Icons.Rounded.Smartphone,
                    isAvailable = false
                ),
                MissionItem(
                    type = MissionType.QR_CODE,
                    title = stringResource(R.string.mission_qr_code),
                    description = "Scan a QR code or barcode",
                    icon = Icons.Rounded.QrCodeScanner,
                    isAvailable = false
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(missionTypes) { item ->
                    val isAvailable = item.isAvailable
                    
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (isAvailable) 1f else 0.5f)
                            .clickable(enabled = isAvailable) {
                                coroutineScope.launch {
                                    sheetState.hide()
                                    onMissionTypeSelected(item.type)
                                }
                            },
                        headlineContent = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                if (!isAvailable) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = MaterialTheme.shapes.extraSmall
                                    ) {
                                        Text(
                                            text = stringResource(R.string.coming_soon),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        },
                        supportingContent = { 
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall
                            ) 
                        },
                        leadingContent = {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = if (isAvailable) 
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isAvailable)
                                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                else null
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    modifier = Modifier.padding(10.dp).size(24.dp),
                                    tint = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

private data class MissionItem(
    val type: MissionType,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isAvailable: Boolean = true
)










