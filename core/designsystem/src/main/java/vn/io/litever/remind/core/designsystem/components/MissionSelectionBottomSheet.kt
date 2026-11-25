package vn.io.litever.remind.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.model.MissionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionSelectionBottomSheet(
    onDismissRequest: () -> Unit,
    onMissionTypeSelected: (MissionType) -> Unit,
    sheetState: SheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
) {
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        ReMindBottomSheetContent(
            title = stringResource(R.string.mission_selection_title),
        ) {
            MissionSelectionBottomSheetContent(
                onMissionTypeSelected = { type ->
                    coroutineScope.launch {
                        sheetState.hide()
                        onMissionTypeSelected(type)
                    }
                }
            )
        }
    }
}

@Composable
fun MissionSelectionBottomSheetContent(
    onMissionTypeSelected: (MissionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = MaterialTheme.spacing.medium)
    ) {
        val missionTypes = listOf(
            MissionItem(
                type = MissionType.TYPING,
                title = stringResource(R.string.mission_typing),
                description = stringResource(R.string.mission_typing_desc),
                icon = ReMindIcons.Keyboard
            ),
            MissionItem(
                type = MissionType.MEMORY_FIND_COLOR_TILES,
                title = stringResource(R.string.mission_memory_tiles),
                description = stringResource(R.string.mission_memory_tiles_desc),
                icon = ReMindIcons.GridView
            ),
            MissionItem(
                type = MissionType.MATH,
                title = stringResource(R.string.mission_math),
                description = stringResource(R.string.mission_math_desc),
                icon = ReMindIcons.Calculate
            ),
            MissionItem(
                type = MissionType.SHAKE,
                title = stringResource(R.string.mission_shake),
                description = stringResource(R.string.mission_shake_desc),
                icon = ReMindIcons.Vibration
            ),
            MissionItem(
                type = MissionType.BARCODE,
                title = stringResource(R.string.mission_barcode),
                description = stringResource(R.string.mission_barcode_desc),
                icon = ReMindIcons.QrCodeScanner
            ),
            MissionItem(
                type = MissionType.RANDOM,
                title = stringResource(R.string.mission_random),
                description = stringResource(R.string.mission_random_desc),
                icon = ReMindIcons.Refresh,
                isProOnly = true
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(missionTypes) { item ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onMissionTypeSelected(item.type)
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
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(MaterialTheme.spacing.smallMedium)
                                    .size(MaterialTheme.spacing.large),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (item.isProOnly) {
                            ReMindProDiamondTag()
                        }
                    }
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
    val isProOnly: Boolean = false
)

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun MissionSelectionBottomSheetContentPreview() {
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        MissionSelectionBottomSheetContent(
            onMissionTypeSelected = {}
        )
    }
}

