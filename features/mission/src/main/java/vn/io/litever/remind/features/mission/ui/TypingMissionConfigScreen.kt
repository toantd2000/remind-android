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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.remind.features.mission.viewmodel.TypingMissionConfigViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Add

@Composable
fun TypingMissionConfigRoute(
    reminderId: Long,
    initialRepetitions: Int = 1,
    initialSelectedPhraseIds: List<Long> = emptyList(),
    onBackClick: () -> Unit,
    onNavigateToPhraseSelection: (List<Long>) -> Unit,
    onSaveMission: (Mission) -> Unit,
    viewModel: TypingMissionConfigViewModel = hiltViewModel()
) {
    var repetitions by rememberSaveable { mutableIntStateOf(initialRepetitions) }
    val basicPhrases by viewModel.basicPhrases.collectAsState()
    
    val randomPhrase = remember(basicPhrases) {
        if (basicPhrases.isNotEmpty()) basicPhrases.random().content else ""
    }

    TypingMissionConfigScreen(
        repetitions = repetitions,
        selectedPhraseIds = initialSelectedPhraseIds,
        randomExamplePhrase = randomPhrase,
        onBackClick = onBackClick,
        onRepetitionsChange = { repetitions = it },
        onNavigateToPhraseSelection = { onNavigateToPhraseSelection(initialSelectedPhraseIds) },
        onSave = {
            onSaveMission(
                Mission(
                    reminderId = reminderId,
                    type = MissionType.TYPING,
                    order = 0, // Will be set by the caller
                    repeatCount = repetitions,
                    config = TypingMissionConfig(initialSelectedPhraseIds)
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
    randomExamplePhrase: String,
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
            // Example Preview Section
            if (randomExamplePhrase.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.mission_typing_example),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = randomExamplePhrase,
                        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Repetitions Input
            Text(
                text = stringResource(R.string.mission_repetitions),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (repetitions > 1) onRepetitionsChange(repetitions - 1) },
                    enabled = repetitions > 1
                ) {
                    Icon(Icons.Rounded.Remove, contentDescription = "Decrease")
                }
                
                OutlinedTextField(
                    value = repetitions.toString(),
                    onValueChange = { newValue ->
                        if (newValue.isEmpty()) {
                            onRepetitionsChange(1)
                        } else {
                            newValue.toIntOrNull()?.let {
                                val coerced = it.coerceIn(1, 99)
                                onRepetitionsChange(coerced)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(100.dp)
                        .padding(horizontal = 16.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    singleLine = true
                )
                
                IconButton(
                    onClick = { if (repetitions < 99) onRepetitionsChange(repetitions + 1) },
                    enabled = repetitions < 99
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Increase")
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
                            text = if (selectedPhraseIds.isEmpty()) 
                                stringResource(R.string.mission_phrases_none_selected) 
                            else 
                                stringResource(R.string.mission_phrases_selected, selectedPhraseIds.size),
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
            randomExamplePhrase = "Wake up and shine",
            onBackClick = {},
            onRepetitionsChange = {},
            onNavigateToPhraseSelection = {},
            onSave = {}
        )
    }
}
