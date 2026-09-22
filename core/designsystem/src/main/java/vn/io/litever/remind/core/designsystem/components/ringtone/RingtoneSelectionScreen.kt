package vn.io.litever.remind.core.designsystem.components.ringtone

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar

@Composable
fun RingtoneSelectionRoute(
    initialUri: String?,
    onBackClick: () -> Unit,
    onRingtoneSelected: (String?) -> Unit,
    viewModel: RingtoneSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { viewModel.addCustomRingtone(it) }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.setInitialSelection(initialUri)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPlayback()
        }
    }

    RingtoneSelectionScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRingtoneClick = { item ->
            viewModel.selectRingtone(item.uri)
        },
        onPickCustomClick = {
            launcher.launch(arrayOf("audio/*"))
        },
        onSaveClick = {
            onRingtoneSelected(uiState.selectedUri)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtoneSelectionScreen(
    uiState: RingtoneSelectionUiState,
    onBackClick: () -> Unit,
    onRingtoneClick: (RingtoneItem) -> Unit,
    onPickCustomClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            LvTopAppBar(
                title = stringResource(R.string.ringtone_selection_title),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ReMindBottomBar {
                LvButton(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.save),
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
                contentPadding = PaddingValues(bottom = LiteverTheme.spacing.medium)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = LiteverTheme.spacing.medium,
                                vertical = LiteverTheme.spacing.small
                            )
                    ) {
                        LvButton(
                            onClick = onPickCustomClick,
                            type = LvButtonType.Outlined,
                            semantic = LvSemantic.Secondary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LibraryMusic,
                                contentDescription = null,
                                modifier = Modifier.size(LiteverTheme.spacing.mediumLarge)
                            )
                            Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))
                            Text(stringResource(R.string.ringtone_custom_pick))
                        }
                    }
                }

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
            .padding(
                horizontal = LiteverTheme.spacing.medium,
                vertical = LiteverTheme.spacing.extraSmall
            )
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() },
        color = if (item.isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.medium,
        border = if (item.isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LiteverTheme.spacing.smallMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = item.isSelected,
                onClick = null,
                modifier = Modifier.size(LiteverTheme.spacing.large)
            )

            Spacer(modifier = Modifier.width(LiteverTheme.spacing.smallMedium))

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
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(LiteverTheme.spacing.mediumLarge)
                )
            }
        }
    }
}
