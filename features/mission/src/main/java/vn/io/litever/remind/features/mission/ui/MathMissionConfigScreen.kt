package vn.io.litever.remind.features.mission.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsDivider
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.core.model.MathDifficulty
import vn.io.litever.remind.core.model.MathMissionConfig
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.features.mission.R
import vn.io.litever.remind.features.mission.ui.components.MissionRadioOptionRow
import vn.io.litever.remind.features.mission.ui.components.MissionStepperGroup

@Composable
fun MathMissionConfigRoute(
    alarmId: Long,
    initialRepetitions: Int = 3,
    initialDifficulty: MathDifficulty = MathDifficulty.NORMAL,
    onBackClick: () -> Unit,
    onSaveMission: (Mission) -> Unit,
) {
    var repetitions by rememberSaveable { mutableIntStateOf(initialRepetitions) }
    var difficulty by rememberSaveable { mutableStateOf(initialDifficulty) }

    MathMissionConfigScreen(
        repetitions = repetitions,
        difficulty = difficulty,
        onBackClick = onBackClick,
        onRepetitionsChange = { repetitions = it },
        onDifficultyChange = { difficulty = it },
        onSave = {
            onSaveMission(
                Mission(
                    alarmId = alarmId,
                    type = MissionType.MATH,
                    order = 0,
                    repeatCount = repetitions,
                    config = MathMissionConfig(difficulty = difficulty)
                )
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathMissionConfigScreen(
    repetitions: Int,
    difficulty: MathDifficulty,
    onBackClick: () -> Unit,
    onRepetitionsChange: (Int) -> Unit,
    onDifficultyChange: (MathDifficulty) -> Unit,
    onSave: () -> Unit
) {
    val difficulties = listOf(
        MathDifficulty.EASY to R.string.math_difficulty_easy,
        MathDifficulty.NORMAL to R.string.math_difficulty_normal,
        MathDifficulty.HARD to R.string.math_difficulty_hard,
        MathDifficulty.VERY_HARD to R.string.math_difficulty_very_hard
    )

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            LvTopAppBar(
                title = stringResource(R.string.math_config_title),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ReMindBottomBar {
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
        ) {
            // Section 1: Difficulty Selection
            ReMindSettingsGroup(
                title = stringResource(R.string.math_difficulty_settings)
            ) {
                difficulties.forEachIndexed { index, (level, titleRes) ->
                    MissionRadioOptionRow(
                        title = stringResource(titleRes),
                        selected = difficulty == level,
                        onClick = { onDifficultyChange(level) }
                    )
                    if (index < difficulties.lastIndex) {
                        ReMindSettingsDivider()
                    }
                }
            }

            // Section 2: Number of problems (Repetitions)
            MissionStepperGroup(
                title = stringResource(R.string.math_problems_count),
                valueText = "$repetitions",
                helperText = stringResource(R.string.math_problems_helper, repetitions),
                canDecrement = repetitions > 1,
                canIncrement = repetitions < 50,
                onDecrement = { onRepetitionsChange(repetitions - 1) },
                onIncrement = { onRepetitionsChange(repetitions + 1) },
                onReset = { onRepetitionsChange(3) }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        }
    }
}

@Preview(name = "Math Config - Light", showBackground = true)
@Composable
private fun MathMissionConfigScreenPreview() {
    ReMindTheme(darkTheme = false) {
        MathMissionConfigScreen(
            repetitions = 3,
            difficulty = MathDifficulty.NORMAL,
            onBackClick = {},
            onRepetitionsChange = {},
            onDifficultyChange = {},
            onSave = {}
        )
    }
}

@Preview(
    name = "Math Config - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF19120CL
)
@Composable
private fun MathMissionConfigScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        MathMissionConfigScreen(
            repetitions = 5,
            difficulty = MathDifficulty.VERY_HARD,
            onBackClick = {},
            onRepetitionsChange = {},
            onDifficultyChange = {},
            onSave = {}
        )
    }
}
