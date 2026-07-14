package vn.io.litever.remind.features.mission.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.components.LiteverCard
import vn.io.litever.designsystem.components.LiteverIconButton
import vn.io.litever.designsystem.components.LiteverTextField
import vn.io.litever.designsystem.components.LiteverTopAppBar
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
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
                LiteverTopAppBar(
                    title = stringResource(R.string.memory_game_config_title),
                    onBackClick = onBackClick
                )
            }
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth()) {
                ReMindBottomBar {
                    LiteverButton(
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
                .background(LiteverTheme.colors.background)
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Section: Difficulty Settings
            Text(
                text = stringResource(R.string.memory_game_difficulty_settings),
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
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val difficultyText = when (gridSize) {
                        3 -> stringResource(R.string.memory_game_difficulty_very_easy)
                        4 -> stringResource(R.string.memory_game_difficulty_easy)
                        5 -> stringResource(R.string.memory_game_difficulty_medium)
                        6 -> stringResource(R.string.memory_game_difficulty_hard)
                        7 -> stringResource(R.string.memory_game_difficulty_very_hard)
                        else -> ""
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiteverIconButton(
                            onClick = { if (gridSize > 3) onGridSizeChange(gridSize - 1) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = if (gridSize > 3) LiteverTheme.colors.primary else LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = difficultyText,
                            style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = LiteverTheme.colors.primary
                        )

                        LiteverIconButton(
                            onClick = { if (gridSize < 7) onGridSizeChange(gridSize + 1) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = if (gridSize < 7) LiteverTheme.colors.primary else LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    MemoryGameStaticPreview(gridSize = gridSize, targetTiles = targetTiles)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Section: Repetitions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.memory_game_repetitions),
                    style = LiteverTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = LiteverTheme.colors.primary,
                        letterSpacing = 1.sp
                    )
                )
                LiteverIconButton(
                    onClick = { onRepetitionsChange(1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Reset",
                        tint = LiteverTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            LiteverCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LiteverTheme.colors.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, LiteverTheme.colors.outlineVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiteverIconButton(
                        onClick = { if (repetitions > 1) onRepetitionsChange(repetitions - 1) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = if (repetitions > 1) LiteverTheme.colors.primary else LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "$repetitions",
                        style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LiteverTheme.colors.primary
                    )

                    LiteverIconButton(
                        onClick = { if (repetitions < 99) onRepetitionsChange(repetitions + 1) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (repetitions < 99) LiteverTheme.colors.primary else LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stringResource(R.string.memory_game_repetition_helper, repetitions),
                style = LiteverTheme.typography.bodySmall,
                color = LiteverTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

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
            .padding(vertical = 8.dp),
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
                                    .padding(2.dp)
                                    .background(
                                        color = if (isTarget) LiteverTheme.colors.primary else LiteverTheme.colors.surfaceVariant,
                                        shape = LiteverTheme.shapes.extraSmall
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
