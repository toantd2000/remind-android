package vn.io.litever.remind.core.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = MaterialTheme.spacing.medium)
            .navigationBarsPadding()
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(missionTypes) { index, item ->
                ReMindSettingsItem(
                    title = item.title,
                    subtitle = item.description,
                    icon = item.icon,
                    iconTint = MaterialTheme.colorScheme.primary,
                    showChevron = true,
                    titleBadge = if (item.isProOnly) {
                        { ReMindProTag() }
                    } else {
                        null
                    },
                    onClick = { onMissionTypeSelected(item.type) }
                )
                if (index < missionTypes.lastIndex) {
                    ReMindSettingsDivider()
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

@Preview(name = "Mission Selection - Light", showBackground = true)
@Composable
private fun MissionSelectionBottomSheetContentPreview() {
    ReMindTheme {
        MissionSelectionBottomSheetContent(onMissionTypeSelected = {})
    }
}

@Preview(
    name = "Mission Selection - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF19120CL
)
@Composable
private fun MissionSelectionBottomSheetContentDarkPreview() {
    ReMindTheme(darkTheme = true) {
        MissionSelectionBottomSheetContent(onMissionTypeSelected = {})
    }
}
