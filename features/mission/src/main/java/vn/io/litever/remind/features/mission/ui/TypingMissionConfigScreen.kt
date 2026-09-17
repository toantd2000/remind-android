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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.button.LvIconButton
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsCategory
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsItem
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.core.model.TypingMissionConfig
import vn.io.litever.remind.core.model.TypingMode
import vn.io.litever.remind.features.mission.R
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
                    order = 0, // Will be set by the caller
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
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            Box(Modifier.fillMaxWidth()) {
                LvTopAppBar(
                    title = stringResource(vn.io.litever.remind.features.mission.R.string.typing_mission_title),
                    onBackClick = onBackClick
                )
            }
        },
        bottomBar = {
            ReMindBottomBar {
                LvButton(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedPhrases.isNotEmpty(),
                    semantic = LvSemantic.Primary
                ) {
                    Text(
                        text = stringResource(vn.io.litever.remind.core.designsystem.R.string.save),
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LiteverTheme.colors.background)
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))

            // Section 1: Phrases
            ReMindSettingsGroup(
                title = stringResource(vn.io.litever.remind.features.mission.R.string.phrases_to_type)
            ) {
                ReMindSettingsItem(
                    title = stringResource(vn.io.litever.remind.features.mission.R.string.phrase_list),
                    subtitle = stringResource(vn.io.litever.remind.features.mission.R.string.phrases_count, selectedPhrases.size),
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                        )
                    },
                    onClick = onNavigateToPhraseSelection
                )

                val visibleCount = if (selectedPhrases.size <= 3) selectedPhrases.size else 2
                selectedPhrases.take(visibleCount).forEach { phrase ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            horizontal = LiteverTheme.spacing.medium,
                            vertical = LiteverTheme.spacing.tiny
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(LiteverTheme.colors.primary.copy(alpha = 0.5f), RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.width(LiteverTheme.spacing.smallMedium))
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
                        modifier = Modifier.padding(start = LiteverTheme.spacing.mediumLarge)
                    )
                }
                Spacer(modifier = Modifier.height(LiteverTheme.spacing.smallMedium))
            }

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))

            // Section 2: Repetitions
            ReMindSettingsGroup {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = LiteverTheme.spacing.medium,
                            bottom = LiteverTheme.spacing.extraSmall,
                            start = LiteverTheme.spacing.medium,
                            end = LiteverTheme.spacing.small
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReMindSettingsCategory(
                        title = stringResource(R.string.settings_title),
                        modifier = Modifier.weight(1f)
                    )
                    LvIconButton(
                        onClick = { onRepetitionsChange(1) },
                        modifier = Modifier.size(32.dp),
                        type = LvButtonType.Text,
                        semantic = LvSemantic.Secondary
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = LiteverTheme.spacing.medium,
                            vertical = LiteverTheme.spacing.small
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LvIconButton(
                        onClick = { onRepetitionsChange(repetitions - 1) },
                        modifier = Modifier.size(40.dp),
                        enabled = repetitions > 1,
                        type = LvButtonType.Text,
                        semantic = LvSemantic.Neutral
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "$repetitions",
                        style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LiteverTheme.colors.neutral
                    )

                    LvIconButton(
                        onClick = { onRepetitionsChange(repetitions + 1) },
                        modifier = Modifier.size(40.dp),
                        enabled = repetitions < 99,
                        type = LvButtonType.Text,
                        semantic = LvSemantic.Neutral
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.repetition_helper, repetitions),
                    style = LiteverTheme.typography.bodySmall,
                    color = LiteverTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(
                        start = LiteverTheme.spacing.medium,
                        end = LiteverTheme.spacing.medium,
                        bottom = LiteverTheme.spacing.medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))

            // Section 3: Modes
            ReMindSettingsGroup(
                title = stringResource(vn.io.litever.remind.core.designsystem.R.string.typing_mode)
            ) {
                val modes = listOf(
                    TypingMode.NORMAL to stringResource(vn.io.litever.remind.core.designsystem.R.string.typing_mode_normal),
                    TypingMode.SHUFFLE_WORDS to stringResource(vn.io.litever.remind.core.designsystem.R.string.typing_mode_shuffle_words),
                    TypingMode.SHUFFLE_CHARS to stringResource(vn.io.litever.remind.core.designsystem.R.string.typing_mode_shuffle_chars)
                )

                modes.forEach { (m, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeChange(m) }
                            .padding(
                                horizontal = LiteverTheme.spacing.medium,
                                vertical = LiteverTheme.spacing.tiny
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == m,
                            onClick = { onModeChange(m) },
                        )
                        Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))
                        Text(
                            text = label,
                            style = LiteverTheme.typography.bodyLarge,
                            color = LiteverTheme.colors.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(LiteverTheme.spacing.smallMedium))
            }
            Spacer(modifier = Modifier.weight(1f))

            // Placeholder for Ad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(bottom = LiteverTheme.spacing.medium)
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
            mode = TypingMode.NORMAL,
            onBackClick = {},
            onRepetitionsChange = {},
            onModeChange = {},
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
            mode = TypingMode.SHUFFLE_WORDS,
            onBackClick = {},
            onRepetitionsChange = {},
            onModeChange = {},
            onNavigateToPhraseSelection = {},
            onSave = {}
        )
    }
}










