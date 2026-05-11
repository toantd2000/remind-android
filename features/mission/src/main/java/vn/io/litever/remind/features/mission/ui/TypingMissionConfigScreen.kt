package vn.io.litever.remind.features.mission.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.designsystem.components.LiteverCard
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.components.ReMindButton
import vn.io.litever.remind.core.designsystem.components.ReMindTopAppBar
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.core.model.TypingMissionConfig
import vn.io.litever.remind.features.mission.viewmodel.TypingMissionConfigViewModel


@Composable
fun TypingMissionConfigRoute(
    alarmId: Long,
    initialRepetitions: Int = 1,
    initialSelectedPhraseIds: List<Long> = emptyList(),
    onBackClick: () -> Unit,
    onNavigateToPhraseSelection: (List<Long>) -> Unit,
    onSaveMission: (Mission) -> Unit,
    viewModel: TypingMissionConfigViewModel = hiltViewModel()
) {
    var repetitions by rememberSaveable { mutableIntStateOf(initialRepetitions) }
    val selectedPhrases by viewModel.selectedPhrases.collectAsState()
    
    LaunchedEffect(initialSelectedPhraseIds) {
        viewModel.loadSelectedPhrases(initialSelectedPhraseIds, alarmId)
    }
    
    TypingMissionConfigScreen(
        repetitions = repetitions,
        selectedPhrases = selectedPhrases,
        onBackClick = onBackClick,
        onRepetitionsChange = { repetitions = it },
        onNavigateToPhraseSelection = { onNavigateToPhraseSelection(initialSelectedPhraseIds) },
        onSave = {
            onSaveMission(
                Mission(
                    alarmId = alarmId,
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
    selectedPhrases: List<Phrase>,
    onBackClick: () -> Unit,
    onRepetitionsChange: (Int) -> Unit,
    onNavigateToPhraseSelection: () -> Unit,
    onSave: () -> Unit
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            Box(Modifier.fillMaxWidth()) {
                ReMindTopAppBar(
                    title = stringResource(vn.io.litever.remind.features.mission.R.string.typing_mission_title),
                    onBackClick = onBackClick
                )
            }
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth()) {
                ReMindBottomBar {
                    ReMindButton(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedPhrases.isNotEmpty()
                    ) {
                        Text(
                            text = stringResource(vn.io.litever.remind.core.designsystem.R.string.save),
                            style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LiteverTheme.colors.background)
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Phrases
            Text(
                text = stringResource(vn.io.litever.remind.features.mission.R.string.phrases_to_type),
                style = LiteverTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = LiteverTheme.colors.primary,
                    letterSpacing = 1.sp
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            LiteverCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LiteverTheme.colors.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, LiteverTheme.colors.outlineVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPhraseSelection() }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(vn.io.litever.remind.features.mission.R.string.phrase_list),
                                style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = stringResource(vn.io.litever.remind.features.mission.R.string.phrases_count, selectedPhrases.size),
                                style = LiteverTheme.typography.bodySmall,
                                color = LiteverTheme.colors.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    if (selectedPhrases.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val visibleCount = if (selectedPhrases.size <= 3) selectedPhrases.size else 2
                            selectedPhrases.take(visibleCount).forEach { phrase ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(LiteverTheme.colors.primary.copy(alpha = 0.5f), RoundedCornerShape(50))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "\"${phrase.content}\"",
                                        style = LiteverTheme.typography.bodyMedium.copy(
                                            fontStyle = FontStyle.Italic,
                                            color = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    )
                                }
                            }
                            if (selectedPhrases.size > visibleCount) {
                                Text(
                                    text = stringResource(vn.io.litever.remind.features.mission.R.string.more_phrases_count, selectedPhrases.size - visibleCount),
                                    style = LiteverTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(start = 18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Section 2: Repetitions
            Text(
                text = stringResource(vn.io.litever.remind.features.mission.R.string.settings_title),
                style = LiteverTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = LiteverTheme.colors.primary,
                    letterSpacing = 1.sp
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            LiteverCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LiteverTheme.colors.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, LiteverTheme.colors.outlineVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_repetitions),
                        style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (repetitions > 1) onRepetitionsChange(repetitions - 1) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Remove,
                                contentDescription = null,
                                tint = LiteverTheme.colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        TextField(
                            value = repetitions.toString(),
                            onValueChange = { newValue ->
                                if (newValue.isEmpty()) {
                                    onRepetitionsChange(1)
                                } else {
                                    newValue.toIntOrNull()?.let {
                                        onRepetitionsChange(it.coerceIn(1, 99))
                                    }
                                }
                            },
                            modifier = Modifier.width(64.dp),
                            textStyle = LiteverTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        
                        IconButton(
                            onClick = { if (repetitions < 99) onRepetitionsChange(repetitions + 1) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = LiteverTheme.colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            
            Text(
                text = stringResource(vn.io.litever.remind.features.mission.R.string.repetition_helper, repetitions),
                style = LiteverTheme.typography.labelSmall,
                color = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp, start = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Placeholder for Ad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TypingMissionConfigScreenPreview() {
    ReMindTheme {
        TypingMissionConfigScreen(
            repetitions = 3,
            selectedPhrases = listOf(
                Phrase(id = 1, content = "I am wide awake", categoryId = "basic", alarmId = 0),
                Phrase(id = 2, content = "Time to conquer the day", categoryId = "basic", alarmId = 0)
            ),
            onBackClick = {},
            onRepetitionsChange = {},
            onNavigateToPhraseSelection = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TypingMissionConfigScreenManyPhrasesPreview() {
    ReMindTheme {
        TypingMissionConfigScreen(
            repetitions = 5,
            selectedPhrases = listOf(
                Phrase(id = 1, content = "I am wide awake", categoryId = "basic", alarmId = 0),
                Phrase(id = 2, content = "Time to conquer the day", categoryId = "basic", alarmId = 0),
                Phrase(id = 3, content = "I will not snooze", categoryId = "basic", alarmId = 0),
                Phrase(id = 4, content = "Morning is beautiful", categoryId = "basic", alarmId = 0),
                Phrase(id = 5, content = "Let's get to work", categoryId = "basic", alarmId = 0)
            ),
            onBackClick = {},
            onRepetitionsChange = {},
            onNavigateToPhraseSelection = {},
            onSave = {}
        )
    }
}










