package vn.io.litever.remind.features.alarms.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import vn.io.litever.remind.core.designsystem.components.ringtone.RingtoneItem
import vn.io.litever.remind.core.designsystem.components.ringtone.RingtoneSelectionRoute as SharedRingtoneSelectionRoute
import vn.io.litever.remind.core.designsystem.components.ringtone.RingtoneSelectionScreen as SharedRingtoneSelectionScreen
import vn.io.litever.remind.core.designsystem.components.ringtone.RingtoneSelectionUiState
import vn.io.litever.remind.core.designsystem.components.ringtone.RingtoneSelectionViewModel

@Composable
fun RingtoneSelectionRoute(
    initialUri: String?,
    onBackClick: () -> Unit,
    onRingtoneSelected: (String?) -> Unit,
    viewModel: RingtoneSelectionViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()
) {
    SharedRingtoneSelectionRoute(
        initialUri = initialUri,
        onBackClick = onBackClick,
        onRingtoneSelected = onRingtoneSelected,
        viewModel = viewModel
    )
}

@Composable
fun RingtoneSelectionScreen(
    uiState: RingtoneSelectionUiState,
    onBackClick: () -> Unit,
    onRingtoneClick: (RingtoneItem) -> Unit,
    onPickCustomClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SharedRingtoneSelectionScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRingtoneClick = onRingtoneClick,
        onPickCustomClick = onPickCustomClick,
        onSaveClick = onSaveClick,
        modifier = modifier
    )
}











