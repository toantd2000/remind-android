package vn.io.litever.remind.features.mission.ui

import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsCategory
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.MemoryTilesMissionConfig
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.features.mission.R

@Composable
fun MemoryGameConfigRoute(
    alarmId: Long,
    initialRepetitions: Int = 1,
    initialGridSize: Int = 3,
    onBackClick: () -> Unit,
    onSaveMission: (Mission) -> Unit,
) {
    var repetitions by rememberSaveable { mutableIntStateOf(initialRepetitions) }
    var gridSize by rememberSaveable { mutableIntStateOf(initialGridSize) }
    
    val targetTiles = when (gridSize) {
        3 -> 3
        4 -> 5
        5 -> 8
        6 -> 7
        7 -> 8
        else -> 3
    }

    MemoryGameConfigScreen(
        repetitions = repetitions,
        gridSize = gridSize,
        targetTiles = targetTiles,
        onBackClick = onBackClick,
        onRepetitionsChange = { repetitions = it },
        onGridSizeChange = { gridSize = it },
        onSave = {
            onSaveMission(
                Mission(
                    alarmId = alarmId,
                    type = MissionType.MEMORY_FIND_COLOR_TILES,
                    order = 0, // Will be set by the caller
                    repeatCount = repetitions,
                    config = MemoryTilesMissionConfig(gridSize, targetTiles)
                )
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryGameConfigScreen(
    repetitions: Int,
    gridSize: Int,
    targetTiles: Int,
    onBackClick: () -> Unit,
    onRepetitionsChange: (Int) -> Unit,
    onGridSizeChange: (Int) -> Unit,
    onSave: () -> Unit
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            Box(Modifier.fillMaxWidth()) {
                LvTopAppBar(
                    title = stringResource(R.string.memory_game_config_title),
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

            // Section 1: Difficulty Settings
            ReMindSettingsGroup(
                title = stringResource(R.string.memory_game_difficulty_settings)
            ) {
                val difficultyText = when (gridSize) {
                    3 -> stringResource(R.string.memory_game_difficulty_very_easy)
                    4 -> stringResource(R.string.memory_game_difficulty_easy)
                    5 -> stringResource(R.string.memory_game_difficulty_medium)
                    6 -> stringResource(R.string.memory_game_difficulty_hard)
                    7 -> stringResource(R.string.memory_game_difficulty_very_hard)
                    else -> ""
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
                        onClick = { if (gridSize > 3) onGridSizeChange(gridSize - 1) },
                        modifier = Modifier.size(40.dp),
                        enabled = gridSize > 3
                    ) {
                        Icon(
                            imageVector = ReMindIcons.KeyboardArrowLeft,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = difficultyText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = { if (gridSize < 7) onGridSizeChange(gridSize + 1) },
                        modifier = Modifier.size(40.dp),
                        enabled = gridSize < 7
                    ) {
                        Icon(
                            imageVector = ReMindIcons.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                MemoryGameStaticPreview(gridSize = gridSize, targetTiles = targetTiles)
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Section 2: Repetitions
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
                        title = stringResource(R.string.memory_game_repetitions),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onRepetitionsChange(1) },
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { onRepetitionsChange(repetitions + 1) },
                        modifier = Modifier.size(40.dp),
                        enabled = repetitions < 99
                    ) {
                        Icon(
                            imageVector = ReMindIcons.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.memory_game_repetition_helper, repetitions),
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
fun MemoryGameStaticPreview(gridSize: Int, targetTiles: Int) {
    val totalTiles = gridSize * gridSize
    val targetIndices = remember(gridSize, targetTiles) {
        (0 until totalTiles).shuffled().take(targetTiles).toSet()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.small),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(200.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (row in 0 until gridSize) {
                    Row(modifier = Modifier.weight(1f)) {
                        for (col in 0 until gridSize) {
                            val index = row * gridSize + col
                            val isTarget = targetIndices.contains(index)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(MaterialTheme.spacing.tiny)
                                    .background(
                                        color = if (isTarget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = MaterialTheme.shapes.extraSmall
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MemoryGameConfigScreenPreview() {
    ReMindTheme {
        MemoryGameConfigScreen(
            repetitions = 3,
            gridSize = 4,
            targetTiles = 5,
            onBackClick = {},
            onRepetitionsChange = {},
            onGridSizeChange = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MemoryGameConfigScreenHardPreview() {
    ReMindTheme {
        MemoryGameConfigScreen(
            repetitions = 5,
            gridSize = 6,
            targetTiles = 7,
            onBackClick = {},
            onRepetitionsChange = {},
            onGridSizeChange = {},
            onSave = {}
        )
    }
}
