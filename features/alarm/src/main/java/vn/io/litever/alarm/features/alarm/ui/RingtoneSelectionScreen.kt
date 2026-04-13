package vn.io.litever.alarm.features.alarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.alarm.core.designsystem.components.AlarmScaffold
import vn.io.litever.alarm.core.designsystem.components.AlarmTopAppBar
import vn.io.litever.alarm.features.alarm.R
import vn.io.litever.alarm.features.alarm.viewmodel.RingtoneItem
import vn.io.litever.alarm.features.alarm.viewmodel.RingtoneSelectionViewModel

@Composable
fun RingtoneSelectionRoute(
    initialUri: String?,
    onBackClick: () -> Unit,
    onRingtoneSelected: (String?) -> Unit,
    viewModel: RingtoneSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setInitialSelection(initialUri)
    }

    RingtoneSelectionScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRingtoneClick = { item ->
            viewModel.selectRingtone(item.uri)
        },
        onSaveClick = {
            onRingtoneSelected(uiState.selectedUri)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtoneSelectionScreen(
    uiState: vn.io.litever.alarm.features.alarm.viewmodel.RingtoneSelectionUiState,
    onBackClick: () -> Unit,
    onRingtoneClick: (RingtoneItem) -> Unit,
    onSaveClick: () -> Unit
) {
    AlarmScaffold(
        topBar = {
            AlarmTopAppBar(
                title = stringResource(R.string.ringtone_selection_title),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium)
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.ringtones) { item ->
                    RingtoneListItem(
                        item = item,
                        onClick = { onRingtoneClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun RingtoneListItem(
    item: RingtoneItem,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { 
            Text(
                text = item.title,
                style = if (item.isSelected) MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)
                        else MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = {
            RadioButton(
                selected = item.isSelected,
                onClick = null // Handle click on the whole row
            )
        },
        trailingContent = {
            if (item.isPlaying) {
                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = if (item.isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            else MaterialTheme.colorScheme.surface
        )
    )
}
