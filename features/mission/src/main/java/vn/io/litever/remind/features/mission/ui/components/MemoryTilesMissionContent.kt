package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
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

/**
 * Giao diện nhiệm vụ Trò chơi ô nhớ (Memory Tiles Mission) theo chuẩn thiết kế Stitch:
 * - Phía trên: Banner hướng dẫn ngắn gọn (MissionInstructionBanner).
 * - Phía dưới: Card trạng thái trò chơi (Đếm ngược ghi nhớ hoặc Tìm ô) + Lưới ô cờ phản hồi màu sắc trực quan.
 *   Loại bỏ badge tiến độ dư thừa vì đã có trên thanh tiến độ chung.
 */
@Composable
fun MemoryTilesMissionContent(
    board: MemoryGameBoard?,
    onProgressChange: (correct: Int, total: Int) -> Unit = { _, _ -> },
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (board == null) return

    var currentBoard by remember(board) { mutableStateOf(board) }
    var gameState by remember(board) { mutableStateOf(MemoryGameState.MEMORIZE) }
    var countdown by remember(board) { mutableStateOf(3) }
    var selectedIndices by remember(board) { mutableStateOf(setOf<Int>()) }

    // Tính số ô người dùng đã chọn đúng
    val correctCount = remember(currentBoard, selectedIndices) {
        selectedIndices.count { currentBoard.targetIndices.contains(it) }
    }

    LaunchedEffect(correctCount, currentBoard.targetTiles) {
        onProgressChange(correctCount, currentBoard.targetTiles)
    }

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
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
    ) {
        // 1. Phía trên: Banner hướng dẫn ngắn gọn
        MissionInstructionBanner(
            icon = Icons.Rounded.GridView,
            requirementText = stringResource(R.string.mission_memory_requirement)
        )

        // 2. Trạng thái giai đoạn (Ghi nhớ / Tìm ô)
        val statusText = if (gameState == MemoryGameState.MEMORIZE) {
            stringResource(R.string.memory_game_memorize_instruction, countdown)
        } else {
            stringResource(R.string.memory_game_playing_instruction)
        }

        val statusColor = when (gameState) {
            MemoryGameState.MEMORIZE -> LiteverTheme.colors.primary
            MemoryGameState.PLAYING -> LiteverTheme.colors.onSurface
            MemoryGameState.SUCCESS -> LiteverTheme.colors.success
            MemoryGameState.FAILURE -> LiteverTheme.colors.error
        }

        Text(
            text = statusText,
            style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = statusColor,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // 3. Khung chứa lưới ô nhớ (Interactive Tiles Grid)
        val boardBorderColor = when (gameState) {
            MemoryGameState.SUCCESS -> LiteverTheme.colors.success
            MemoryGameState.FAILURE -> LiteverTheme.colors.error
            else -> LiteverTheme.colors.outlineVariant
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = LiteverTheme.colors.surfaceContainerLowest
            ),
            shape = LiteverTheme.shapes.large,
            border = BorderStroke(1.5.dp, boardBorderColor)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(currentBoard.gridSize),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LiteverTheme.spacing.medium)
                    .aspectRatio(1f),
                horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small),
                userScrollEnabled = false
            ) {
                items(currentBoard.gridSize * currentBoard.gridSize) { index ->
                    val isTarget = currentBoard.targetIndices.contains(index)
                    val isSelected = selectedIndices.contains(index)
                    val isClickable = gameState == MemoryGameState.PLAYING && !isSelected

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
                                gameState = MemoryGameState.FAILURE
                            } else {
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MemoryTilesMissionContentPreview() {
    ReMindTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MemoryTilesMissionContent(
                board = MemoryGameBoard(
                    gridSize = 3,
                    targetTiles = 3,
                    targetIndices = listOf(0, 4, 8)
                ),
                onSuccess = {}
            )
        }
    }
}

