package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.model.MemoryGameBoard
import vn.io.litever.remind.features.mission.R

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
        if (gameState == MemoryGameState.MEMORIZE) {
            countdown = 3
            selectedIndices = emptySet()
            while (countdown > 0) {
                delay(1000L)
                countdown--
            }
            gameState = MemoryGameState.PLAYING
        } else if (gameState == MemoryGameState.SUCCESS) {
            delay(1000L)
            onSuccess()
        } else if (gameState == MemoryGameState.FAILURE) {
            delay(1000L)
            gameState = MemoryGameState.MEMORIZE
            currentBoard = currentBoard.copy(
                targetIndices = (0 until (currentBoard.gridSize * currentBoard.gridSize))
                    .shuffled()
                    .take(currentBoard.targetTiles)
            )
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

        Spacer(modifier = Modifier.height(16.dp))

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
                .padding(horizontal = 16.dp)
                .aspectRatio(1f)
                .border(2.dp, outlineColor, LiteverTheme.shapes.medium),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(currentBoard.gridSize * currentBoard.gridSize) { index ->
                val isTarget = currentBoard.targetIndices.contains(index)
                val isSelected = selectedIndices.contains(index)
                
                // Determine color
                val tileColor = when {
                    gameState == MemoryGameState.MEMORIZE && isTarget -> LiteverTheme.colors.primary
                    gameState != MemoryGameState.MEMORIZE && isSelected && isTarget -> LiteverTheme.colors.primary
                    gameState != MemoryGameState.MEMORIZE && isSelected && !isTarget -> LiteverTheme.colors.error
                    else -> LiteverTheme.colors.surfaceVariant
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(LiteverTheme.shapes.medium)
                        .background(tileColor)
                        .clickable(enabled = gameState == MemoryGameState.PLAYING && !isSelected) {
                            if (gameState == MemoryGameState.PLAYING) {
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
                            }
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            color = LiteverTheme.colors.primaryContainer.copy(alpha = 0.5f),
            shape = LiteverTheme.shapes.extraSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.mission_progress, currentRepetition, totalRepetitions),
                style = LiteverTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = LiteverTheme.colors.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}
