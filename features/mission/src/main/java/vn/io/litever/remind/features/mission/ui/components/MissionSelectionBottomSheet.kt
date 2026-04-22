package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.designsystem.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionSelectionBottomSheet(
    onDismissRequest: () -> Unit,
    onMissionTypeSelected: (MissionType) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.mission_selection_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp)
            )
            
            val missionTypes = listOf(
                MissionItem(MissionType.TYPING, stringResource(R.string.mission_typing), stringResource(R.string.mission_typing_desc), Icons.Rounded.Keyboard),
                MissionItem(MissionType.MATH, stringResource(R.string.mission_math), stringResource(R.string.mission_math_desc), Icons.Rounded.Calculate),
                MissionItem(MissionType.SHAKE, stringResource(R.string.mission_shake), "Shake your phone to wake up", Icons.Rounded.Vibration),
                MissionItem(MissionType.QR_CODE, stringResource(R.string.mission_qr_code), "Scan a QR code or barcode", Icons.Rounded.QrCodeScanner)
            )

            LazyColumn {
                items(missionTypes) { item ->
                    ListItem(
                        headlineContent = { Text(item.title) },
                        supportingContent = { Text(item.description) },
                        leadingContent = {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    modifier = Modifier.padding(8.dp).size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            onMissionTypeSelected(item.type)
                        }
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
    val icon: ImageVector
)
