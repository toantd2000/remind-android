package vn.io.litever.remind.features.reminder.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.features.reminder.R
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.remind.core.designsystem.components.ReMindScaffold
import vn.io.litever.remind.core.designsystem.components.ReMindTopAppBar
import vn.io.litever.remind.features.reminder.viewmodel.RingtoneItem
import vn.io.litever.remind.features.reminder.viewmodel.RingtoneSelectionViewModel
import vn.io.litever.remind.core.designsystem.components.*

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
    uiState: vn.io.litever.remind.features.reminder.viewmodel.RingtoneSelectionUiState,
    onBackClick: () -> Unit,
    onRingtoneClick: (RingtoneItem) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ReMindScaffold(
        modifier = modifier,
        topBar = {
            ReMindTopAppBar(
                title = stringResource(R.string.ringtone_selection_title),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ReMindBottomBar {
                ReMindButton(
                    onClick = onSaveClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.save),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() },
        color = if (item.isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.medium,
        border = if (item.isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                 else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = item.isSelected,
                onClick = null,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = item.title,
                modifier = Modifier.weight(1f),
                style = if (item.isSelected) MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                ) else MaterialTheme.typography.bodyLarge
            )
            
            if (item.isPlaying) {
                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
