package vn.io.litever.remind.features.mission.ui

import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsCategory
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.MathDifficulty
import vn.io.litever.remind.core.model.MathMissionConfig
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.features.mission.R

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
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            Box(Modifier.fillMaxWidth()) {
                LvTopAppBar(
                    title = stringResource(R.string.math_config_title),
                    onBackClick = onBackClick
                )
            }
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth()) {
                ReMindBottomBar {
                    Button(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.mission_complete))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Section 1: Difficulty Selection
            ReMindSettingsGroup(
                title = stringResource(R.string.math_difficulty_settings)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.small)
                ) {
                    DifficultyOptionRow(
                        title = stringResource(R.string.math_difficulty_easy),
                        selected = difficulty == MathDifficulty.EASY,
                        onClick = { onDifficultyChange(MathDifficulty.EASY) }
                    )
                    DifficultyOptionRow(
                        title = stringResource(R.string.math_difficulty_normal),
                        selected = difficulty == MathDifficulty.NORMAL,
                        onClick = { onDifficultyChange(MathDifficulty.NORMAL) }
                    )
                    DifficultyOptionRow(
                        title = stringResource(R.string.math_difficulty_hard),
                        selected = difficulty == MathDifficulty.HARD,
                        onClick = { onDifficultyChange(MathDifficulty.HARD) }
                    )
                    DifficultyOptionRow(
                        title = stringResource(R.string.math_difficulty_very_hard),
                        selected = difficulty == MathDifficulty.VERY_HARD,
                        onClick = { onDifficultyChange(MathDifficulty.VERY_HARD) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Section 2: Number of problems (Repetitions)
            ReMindSettingsGroup {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = MaterialTheme.spacing.medium,
                            bottom = MaterialTheme.spacing.extraSmall,
                            start = MaterialTheme.spacing.medium,
                            end = MaterialTheme.spacing.small
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReMindSettingsCategory(
                        title = stringResource(R.string.math_problems_count),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onRepetitionsChange(3) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = ReMindIcons.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.small
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onRepetitionsChange(repetitions - 1) },
                        modifier = Modifier.size(40.dp),
                        enabled = repetitions > 1
                    ) {
                        Icon(
                            imageVector = ReMindIcons.KeyboardArrowLeft,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "$repetitions",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { onRepetitionsChange(repetitions + 1) },
                        modifier = Modifier.size(40.dp),
                        enabled = repetitions < 50
                    ) {
                        Icon(
                            imageVector = ReMindIcons.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.math_problems_helper, repetitions),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        start = MaterialTheme.spacing.medium,
                        end = MaterialTheme.spacing.medium,
                        bottom = MaterialTheme.spacing.medium
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DifficultyOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.small
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Light Mode")
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

@Preview(name = "Dark Mode")
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
