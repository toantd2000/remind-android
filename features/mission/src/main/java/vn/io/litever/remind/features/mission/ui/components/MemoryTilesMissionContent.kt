package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.MemoryGameBoard
import vn.io.litever.remind.features.mission.R
import kotlin.time.Duration.Companion.milliseconds

enum class MemoryGameState {
    MEMORIZE, PLAYING, SUCCESS, FAILURE
}

@Composable
fun MemoryTilesMissionContent(
    board: MemoryGameBoard?,
    currentRepetition: Int,
    totalRepetitions: Int,
    onSuccess: () -> Unit
) {
    if (board == null) return

    var currentBoard by remember(board) { mutableStateOf(board) }
    var gameState by remember(board) { mutableStateOf(MemoryGameState.MEMORIZE) }
    var countdown by remember(board) { mutableStateOf(3) }
    var selectedIndices by remember(board) { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(gameState, currentBoard) {
        when (gameState) {
            MemoryGameState.MEMORIZE -> {
                countdown = 3
                selectedIndices = emptySet()
                while (countdown > 0) {
                    delay(1000L.milliseconds)
                    countdown--
                }
                gameState = MemoryGameState.PLAYING
            }
            MemoryGameState.SUCCESS -> {
                delay(1000L.milliseconds)
                onSuccess()
            }
            MemoryGameState.FAILURE -> {
                delay(1000L.milliseconds)
                gameState = MemoryGameState.MEMORIZE
                currentBoard = currentBoard.copy(
                    targetIndices = (0 until (currentBoard.gridSize * currentBoard.gridSize))
                        .shuffled()
                        .take(currentBoard.targetTiles)
                )
            }

            else -> {}
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (gameState == MemoryGameState.MEMORIZE) 
                stringResource(R.string.memory_game_memorize_instruction, countdown)
            else 
                stringResource(R.string.memory_game_playing_instruction),
            style = LiteverTheme.typography.titleMedium,
            color = LiteverTheme.colors.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))

        val outlineColor = when (gameState) {
            MemoryGameState.SUCCESS -> LiteverTheme.colors.success
            MemoryGameState.FAILURE -> LiteverTheme.colors.error
            else -> LiteverTheme.colors.outlineVariant
        }

        // Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(currentBoard.gridSize),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LiteverTheme.spacing.medium)
                .aspectRatio(1f)
                .border(LiteverTheme.spacing.tiny, outlineColor, LiteverTheme.shapes.large),
            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small),
            contentPadding = PaddingValues(LiteverTheme.spacing.medium)
        ) {
            items(currentBoard.gridSize * currentBoard.gridSize) { index ->
                val isTarget = currentBoard.targetIndices.contains(index)
                val isSelected = selectedIndices.contains(index)
                val isClickable = gameState == MemoryGameState.PLAYING && !isSelected
                
                // Determine semantic color intent
                val tileSemantic = when {
                    gameState == MemoryGameState.MEMORIZE && isTarget -> LvSemantic.Primary
                    gameState != MemoryGameState.MEMORIZE && isSelected && isTarget -> LvSemantic.Primary
                    gameState != MemoryGameState.MEMORIZE && isSelected && !isTarget -> LvSemantic.Destructive
                    else -> LvSemantic.Neutral
                }

                val tileColor = when (tileSemantic) {
                    LvSemantic.Primary -> MaterialTheme.colorScheme.primary
                    LvSemantic.Destructive -> MaterialTheme.colorScheme.error
                    else -> LiteverTheme.colors.surfaceVariant
                }

                val tileStateDescription = when {
                    gameState == MemoryGameState.MEMORIZE && isTarget -> "Target"
                    gameState != MemoryGameState.MEMORIZE && isSelected && isTarget -> "Correct"
                    gameState != MemoryGameState.MEMORIZE && isSelected && !isTarget -> "Wrong"
                    else -> "Unselected"
                }

                LvButton(
                    onClick = {
                        selectedIndices = selectedIndices + index
                        if (!isTarget) {
                            // Wrong! Change to failure state
                            gameState = MemoryGameState.FAILURE
                        } else {
                            // Correct! Check if all found
                            val foundAll = currentBoard.targetIndices.all { selectedIndices.contains(it) || it == index }
                            if (foundAll) {
                                gameState = MemoryGameState.SUCCESS
                            }
                        }
                    },
                    enabled = isClickable,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tileColor,
                        disabledContainerColor = tileColor
                    ),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .semantics {
                            selected = isSelected
                            stateDescription = tileStateDescription
                        },
                    semantic = tileSemantic,
                    contentPadding = PaddingValues(0.dp)
                ) {}
            }
        }

        Spacer(modifier = Modifier.height(LiteverTheme.spacing.large))

        Surface(
            color = LiteverTheme.colors.primaryContainer.copy(alpha = 0.5f),
            shape = LiteverTheme.shapes.extraSmall,
            modifier = Modifier.padding(bottom = LiteverTheme.spacing.large)
        ) {
            Text(
                text = stringResource(R.string.mission_progress, currentRepetition, totalRepetitions),
                style = LiteverTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = LiteverTheme.colors.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = LiteverTheme.spacing.smallMedium, vertical = LiteverTheme.spacing.extraSmall)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MemoryTilesMissionContentPreview() {
    ReMindTheme {
        Box(modifier = Modifier.padding(LiteverTheme.spacing.medium)) {
            MemoryTilesMissionContent(
                board = MemoryGameBoard(
                    gridSize = 3,
                    targetTiles = 3,
                    targetIndices = listOf(0, 4, 8)
                ),
                currentRepetition = 1,
                totalRepetitions = 3,
                onSuccess = {}
            )
        }
    }
}
