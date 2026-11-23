package vn.io.litever.remind.features.mission.ui

import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.MathDifficulty
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.RandomMissionConfig
import vn.io.litever.remind.features.mission.R
import javax.inject.Inject

@HiltViewModel
class RandomMissionConfigViewModel @Inject constructor(
    preferencesDataSource: AlarmPreferencesDataSource
) : ViewModel() {
    val isPro: StateFlow<Boolean> = preferencesDataSource.isProUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
}

@Composable
fun RandomMissionConfigRoute(
    alarmId: Long,
    initialCandidateTypes: List<MissionType>,
    initialDifficulty: MathDifficulty,
    initialRepetitions: Int,
    onBackClick: () -> Unit,
    onNavigateToPaywall: (Int) -> Unit,
    onSaveMission: (Mission) -> Unit,
    viewModel: RandomMissionConfigViewModel = hiltViewModel()
) {
    val isPro by viewModel.isPro.collectAsState()
    RandomMissionConfigScreen(
        initialCandidateTypes = initialCandidateTypes,
        initialDifficulty = initialDifficulty,
        initialRepetitions = initialRepetitions,
        isPro = isPro,
        onBackClick = onBackClick,
        onSaveClick = { candidates, difficulty, reps ->
            if (!isPro) {
                onNavigateToPaywall(1)
            } else {
                onSaveMission(
                    Mission(
                        alarmId = alarmId,
                        type = MissionType.RANDOM,
                        order = 0,
                        repeatCount = reps,
                        config = RandomMissionConfig(
                            candidateTypes = candidates,
                            difficulty = difficulty
                        )
                    )
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomMissionConfigScreen(
    initialCandidateTypes: List<MissionType> = listOf(
        MissionType.MATH,
        MissionType.TYPING,
        MissionType.MEMORY_FIND_COLOR_TILES,
        MissionType.SHAKE
    ),
    initialDifficulty: MathDifficulty = MathDifficulty.NORMAL,
    initialRepetitions: Int = 1,
    isPro: Boolean = false,
    onBackClick: () -> Unit,
    onSaveClick: (List<MissionType>, MathDifficulty, Int) -> Unit
) {
    var selectedTypes by remember {
        mutableStateOf(
            initialCandidateTypes.ifEmpty {
                listOf(MissionType.MATH, MissionType.TYPING, MissionType.MEMORY_FIND_COLOR_TILES, MissionType.SHAKE)
            }
        )
    }
    var difficulty by remember { mutableStateOf(initialDifficulty) }
    var repetitions by remember { mutableIntStateOf(initialRepetitions.coerceIn(1, 5)) }

    val availableCandidates = listOf(
        MissionType.MATH to vn.io.litever.remind.core.designsystem.R.string.mission_math,
        MissionType.TYPING to vn.io.litever.remind.core.designsystem.R.string.mission_typing,
        MissionType.MEMORY_FIND_COLOR_TILES to vn.io.litever.remind.core.designsystem.R.string.mission_memory_tiles,
        MissionType.SHAKE to vn.io.litever.remind.core.designsystem.R.string.mission_shake
    )

    Scaffold(
        topBar = {
            LvTopAppBar(
                title = stringResource(R.string.random_config_title),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ReMindBottomBar {
                Button(
                    onClick = { onSaveClick(selectedTypes, difficulty, repetitions) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!isPro) {
                        Icon(
                            imageVector = ReMindIcons.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(stringResource(R.string.save_mission_action))
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
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            ReMindSettingsGroup(title = stringResource(R.string.random_candidates_label)) {
                availableCandidates.forEach { (type, labelRes) ->
                    val checked = selectedTypes.contains(type)
                    ListItem(
                        trailingContent = {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    selectedTypes = if (isChecked) {
                                        (selectedTypes + type).distinct()
                                    } else if (selectedTypes.size > 1) {
                                        selectedTypes - type
                                    } else {
                                        selectedTypes
                                    }
                                }
                            )
                        },
                        modifier = Modifier.clickable {
                            selectedTypes = if (!checked) {
                                (selectedTypes + type).distinct()
                            } else if (selectedTypes.size > 1) {
                                selectedTypes - type
                            } else {
                                selectedTypes
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }

            ReMindSettingsGroup(title = stringResource(R.string.random_difficulty_label)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MathDifficulty.entries.forEach { level ->
                        val label = when (level) {
                            MathDifficulty.EASY -> stringResource(R.string.shake_sensitivity_easy)
                            MathDifficulty.NORMAL -> stringResource(R.string.shake_sensitivity_normal)
                            MathDifficulty.HARD -> stringResource(R.string.shake_sensitivity_hard)
                            MathDifficulty.VERY_HARD -> "Pro+"
                        }
                        FilterChip(
                            selected = difficulty == level,
                            onClick = { difficulty = level },
                            label = { Text(label, maxLines = 1) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            ReMindSettingsGroup(title = stringResource(R.string.random_repetitions_label)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 3, 5).forEach { count ->
                        FilterChip(
                            selected = repetitions == count,
                            onClick = { repetitions = count },
                            label = { Text("${count}x") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RandomMissionConfigScreenPreview() {
    ReMindTheme {
        RandomMissionConfigScreen(
            isPro = false,
            onBackClick = {},
            onSaveClick = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RandomMissionConfigScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        RandomMissionConfigScreen(
            isPro = true,
            onBackClick = {},
            onSaveClick = { _, _, _ -> }
        )
    }
}
