package vn.io.litever.remind.features.mission.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.remind.core.designsystem.components.ReMindScaffold
import vn.io.litever.remind.core.designsystem.components.ReMindTopAppBar
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.TypingMissionConfig
import vn.io.litever.remind.core.designsystem.R

@Composable
fun TypingMissionConfigRoute(
    reminderId: Long,
    initialRepetitions: Int = 1,
    initialSelectedPhraseIds: List<Long> = emptyList(),
    onBackClick: () -> Unit,
    onNavigateToPhraseSelection: (List<Long>) -> Unit,
    onSaveMission: (Mission) -> Unit
) {
    var repetitions by remember { mutableIntStateOf(initialRepetitions) }
    var selectedPhraseIds by remember { mutableStateOf(initialSelectedPhraseIds) }

    TypingMissionConfigScreen(
        repetitions = repetitions,
        selectedPhraseIds = selectedPhraseIds,
        onBackClick = onBackClick,
        onRepetitionsChange = { repetitions = it },
        onNavigateToPhraseSelection = { onNavigateToPhraseSelection(selectedPhraseIds) },
        onSave = {
            onSaveMission(
                Mission(
                    reminderId = reminderId,
                    type = MissionType.TYPING,
                    order = 0, // Will be set by the caller
                    repeatCount = repetitions,
                    config = TypingMissionConfig(selectedPhraseIds)
                )
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypingMissionConfigScreen(
    repetitions: Int,
    selectedPhraseIds: List<Long>,
    onBackClick: () -> Unit,
    onRepetitionsChange: (Int) -> Unit,
    onNavigateToPhraseSelection: () -> Unit,
    onSave: () -> Unit
) {
    ReMindScaffold(
        topBar = {
            ReMindTopAppBar(
                title = stringResource(R.string.mission_typing),
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Repetitions Picker (Simplified)
            Text(
                text = stringResource(R.string.mission_repetitions),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(1, 2, 3, 5).forEach { value ->
                    FilterChip(
                        selected = repetitions == value,
                        onClick = { onRepetitionsChange(value) },
                        label = { Text(stringResource(R.string.times_unit, value)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Phrase Selection Link
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable { onNavigateToPhraseSelection() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.mission_select_phrases),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (selectedPhraseIds.isEmpty()) "None selected" else "${selectedPhraseIds.size} phrases selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge,
                enabled = selectedPhraseIds.isNotEmpty()
            ) {
                Text(
                    text = stringResource(R.string.mission_complete),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun TypingMissionConfigScreenPreview() {
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        TypingMissionConfigScreen(
            repetitions = 3,
            selectedPhraseIds = listOf(1, 2),
            onBackClick = {},
            onRepetitionsChange = {},
            onNavigateToPhraseSelection = {},
            onSave = {}
        )
    }
}
