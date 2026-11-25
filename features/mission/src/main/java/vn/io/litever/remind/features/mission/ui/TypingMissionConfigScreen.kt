package vn.io.litever.remind.features.mission.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsDivider
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsItem
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.core.model.TypingMissionConfig
import vn.io.litever.remind.core.model.TypingMode
import vn.io.litever.remind.features.mission.R
import vn.io.litever.remind.features.mission.ui.components.MissionRadioOptionRow
import vn.io.litever.remind.features.mission.ui.components.MissionStepperGroup
import vn.io.litever.remind.features.mission.viewmodel.TypingMissionConfigViewModel

@Composable
fun TypingMissionConfigRoute(
    alarmId: Long,
    initialRepetitions: Int = 1,
    initialSelectedPhraseIds: List<Long> = emptyList(),
    initialMode: TypingMode = TypingMode.NORMAL,
    onBackClick: () -> Unit,
    onNavigateToPhraseSelection: (List<Long>) -> Unit,
    onSaveMission: (Mission) -> Unit,
    viewModel: TypingMissionConfigViewModel = hiltViewModel()
) {
    var repetitions by rememberSaveable { mutableIntStateOf(initialRepetitions) }
    var mode by rememberSaveable { mutableStateOf(initialMode) }
    val selectedPhrases by viewModel.selectedPhrases.collectAsState()

    LaunchedEffect(initialSelectedPhraseIds) {
        viewModel.loadSelectedPhrases(initialSelectedPhraseIds, alarmId)
    }

    TypingMissionConfigScreen(
        repetitions = repetitions,
        selectedPhrases = selectedPhrases,
        mode = mode,
        onBackClick = onBackClick,
        onRepetitionsChange = { repetitions = it },
        onModeChange = { mode = it },
        onNavigateToPhraseSelection = { onNavigateToPhraseSelection(initialSelectedPhraseIds) },
        onSave = {
            onSaveMission(
                Mission(
                    alarmId = alarmId,
                    type = MissionType.TYPING,
                    order = 0,
                    repeatCount = repetitions,
                    config = TypingMissionConfig(initialSelectedPhraseIds, mode)
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
    mode: TypingMode,
    onBackClick: () -> Unit,
    onRepetitionsChange: (Int) -> Unit,
    onModeChange: (TypingMode) -> Unit,
    onNavigateToPhraseSelection: () -> Unit,
    onSave: () -> Unit
) {
    val modes = listOf(
        TypingMode.NORMAL to vn.io.litever.remind.core.designsystem.R.string.typing_mode_normal,
        TypingMode.SHUFFLE_WORDS to vn.io.litever.remind.core.designsystem.R.string.typing_mode_shuffle_words,
        TypingMode.SHUFFLE_CHARS to vn.io.litever.remind.core.designsystem.R.string.typing_mode_shuffle_chars
    )

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            LvTopAppBar(
                title = stringResource(R.string.typing_mission_title),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ReMindBottomBar {
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedPhrases.isNotEmpty()
                ) {
                    Text(text = stringResource(R.string.save_mission_action))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
        ) {
            // Section 1: Phrases
            ReMindSettingsGroup(
                title = stringResource(R.string.phrases_to_type)
            ) {
                ReMindSettingsItem(
                    title = stringResource(R.string.phrase_list),
                    subtitle = stringResource(R.string.phrases_count, selectedPhrases.size),
                    showChevron = true,
                    onClick = onNavigateToPhraseSelection
                )

                val visibleCount = if (selectedPhrases.size <= 3) selectedPhrases.size else 2
                selectedPhrases.take(visibleCount).forEach { phrase ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.tiny
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.smallMedium))
                        Text(
                            text = "\"${phrase.content}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        )
                    }
                }
                if (selectedPhrases.size > visibleCount) {
                    Text(
                        text = stringResource(R.string.more_phrases_count, selectedPhrases.size - visibleCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.padding(
                            start = MaterialTheme.spacing.mediumLarge,
                            top = MaterialTheme.spacing.tiny
                        )
                    )
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))
            }

            // Section 2: Repetitions
            MissionStepperGroup(
                title = stringResource(R.string.settings_title),
                valueText = "$repetitions",
                helperText = stringResource(R.string.repetition_helper, repetitions),
                canDecrement = repetitions > 1,
                canIncrement = repetitions < 99,
                onDecrement = { onRepetitionsChange(repetitions - 1) },
                onIncrement = { onRepetitionsChange(repetitions + 1) },
                onReset = { onRepetitionsChange(1) }
            )

            // Section 3: Modes
            ReMindSettingsGroup(
                title = stringResource(vn.io.litever.remind.core.designsystem.R.string.typing_mode)
            ) {
                modes.forEachIndexed { index, (m, labelRes) ->
                    MissionRadioOptionRow(
                        title = stringResource(labelRes),
                        selected = mode == m,
                        onClick = { onModeChange(m) }
                    )
                    if (index < modes.lastIndex) {
                        ReMindSettingsDivider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        }
    }
}

@Preview(name = "Typing Config - Light", showBackground = true)
@Composable
fun TypingMissionConfigScreenPreview() {
    ReMindTheme {
        TypingMissionConfigScreen(
            repetitions = 3,
            selectedPhrases = listOf(
                Phrase(id = 1, content = "I am wide awake", categoryId = "basic", alarmId = 0),
                Phrase(id = 2, content = "Time to conquer the day", categoryId = "basic", alarmId = 0)
            ),
            mode = TypingMode.NORMAL,
            onBackClick = {},
            onRepetitionsChange = {},
            onModeChange = {},
            onNavigateToPhraseSelection = {},
            onSave = {}
        )
    }
}

@Preview(
    name = "Typing Config - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF19120CL
)
@Composable
fun TypingMissionConfigScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        TypingMissionConfigScreen(
            repetitions = 5,
            selectedPhrases = listOf(
                Phrase(id = 1, content = "I am wide awake", categoryId = "basic", alarmId = 0),
                Phrase(id = 2, content = "Time to conquer the day", categoryId = "basic", alarmId = 0),
                Phrase(id = 3, content = "I will not snooze", categoryId = "basic", alarmId = 0),
                Phrase(id = 4, content = "Morning is beautiful", categoryId = "basic", alarmId = 0)
            ),
            mode = TypingMode.SHUFFLE_WORDS,
            onBackClick = {},
            onRepetitionsChange = {},
            onModeChange = {},
            onNavigateToPhraseSelection = {},
            onSave = {}
        )
    }
}
