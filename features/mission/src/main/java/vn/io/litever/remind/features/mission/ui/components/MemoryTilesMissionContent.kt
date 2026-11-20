package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.MemoryGameBoard
import vn.io.litever.remind.features.mission.R
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

enum class MemoryGameState {
    MEMORIZE, PLAYING, SUCCESS, FAILURE
}

private const val MAX_LIVES = 3

/**
 * Giao diện nhiệm vụ Trò chơi ô nhớ (Memory Tiles Mission) theo chuẩn Gamification:
 * - Pha Ghi nhớ: Lật mở các ô mục tiêu 3D kèm thanh tiến độ rút dần trong 3s.
 * - Pha Đang chơi: 3 Mạng (❤️❤️❤️), rung + lật 3D + nhún nhẹ khi chạm.
 * - Chạm sai: Lắc ngang ô, báo đỏ, trừ 1 mạng (tim mờ dần).
 * - Pha Thất bại (Reveal All): Khi hết 3 mạng, lật mở toàn bộ ma trận phân loại Rõ (Đã tìm ✓, Chọn nhầm ✗, Bỏ sót ★).
 */
@Composable
fun MemoryTilesMissionContent(
    board: MemoryGameBoard?,
    onUserInteraction: () -> Unit = {},
    onTimerPauseChange: (Boolean) -> Unit = {},
    onContinue: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (board == null) return

    val haptic = LocalHapticFeedback.current
    var currentBoard by remember(board) { mutableStateOf(board) }
    var gameState by remember(board) { mutableStateOf(MemoryGameState.MEMORIZE) }
    var livesRemaining by remember(board) { mutableIntStateOf(MAX_LIVES) }
    var countdown by remember(board) { mutableIntStateOf(3) }
    var selectedIndices by remember(board) { mutableStateOf(setOf<Int>()) }

    // Số ô tìm đúng
    val correctCount = remember(currentBoard, selectedIndices) {
        selectedIndices.count { currentBoard.targetIndices.contains(it) }
    }

    val resetRound = {
        val totalTiles = currentBoard.gridSize * currentBoard.gridSize
        currentBoard = currentBoard.copy(
            targetIndices = (0 until totalTiles).shuffled().take(currentBoard.targetTiles)
        )
        selectedIndices = emptySet()
        livesRemaining = MAX_LIVES
        gameState = MemoryGameState.MEMORIZE
    }

    LaunchedEffect(gameState) {
        val shouldPauseTimer = gameState != MemoryGameState.PLAYING
        onTimerPauseChange(shouldPauseTimer)
    }

    // Điều phối vòng đời các pha trò chơi
    LaunchedEffect(gameState, currentBoard) {
        when (gameState) {
            MemoryGameState.MEMORIZE -> {
                countdown = 3
                selectedIndices = emptySet()
                delay(300L) // Nhịp đệm lật mở ban đầu
                while (countdown > 0) {
                    delay(1000L.milliseconds)
                    countdown--
                }
                gameState = MemoryGameState.PLAYING
            }
            MemoryGameState.SUCCESS -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                // Giữ màn hình ở trạng thái thành công để user bấm Tiếp tục ở Bottom Bar
            }
            MemoryGameState.FAILURE -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                // Lật mở ma trận trong 2.5s rồi tự động làm mới vòng
                delay(2500L)
                if (gameState == MemoryGameState.FAILURE) {
                    val totalTiles = currentBoard.gridSize * currentBoard.gridSize
                    currentBoard = currentBoard.copy(
                        targetIndices = (0 until totalTiles).shuffled().take(currentBoard.targetTiles)
                    )
                    selectedIndices = emptySet()
                    livesRemaining = MAX_LIVES
                    gameState = MemoryGameState.MEMORIZE
                }
            }
            else -> {}
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
    ) {
        // 1. Phía trên: Banner hướng dẫn
        MissionInstructionBanner(
            icon = ReMindIcons.GridView,
            requirementText = stringResource(R.string.mission_memory_requirement)
        )

        // Vùng chơi căn giữa theo chiều dọc
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium, Alignment.CenterVertically)
        ) {

        // 2. Thẻ hiển thị Trạng thái + 3 Mạng (❤️❤️❤️)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = LiteverTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = LiteverTheme.colors.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LiteverTheme.spacing.medium, vertical = LiteverTheme.spacing.smallMedium),
                verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusText = when (gameState) {
                        MemoryGameState.MEMORIZE -> stringResource(R.string.memory_game_memorize_instruction, countdown)
                        MemoryGameState.PLAYING -> stringResource(R.string.memory_game_playing_instruction)
                        MemoryGameState.SUCCESS -> stringResource(R.string.memory_game_round_success)
                        MemoryGameState.FAILURE -> stringResource(
                            R.string.memory_game_round_failed_overview,
                            correctCount,
                            currentBoard.targetTiles
                        )
                    }

                    val statusColor = when (gameState) {
                        MemoryGameState.MEMORIZE -> LiteverTheme.colors.primary
                        MemoryGameState.PLAYING -> LiteverTheme.colors.onSurface
                        MemoryGameState.SUCCESS -> LiteverTheme.colors.success
                        MemoryGameState.FAILURE -> LiteverTheme.colors.error
                    }

                    Text(
                        text = statusText,
                        style = LiteverTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.weight(1f)
                    )

                    // Hiển thị 3 mạng ❤️❤️❤️
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..MAX_LIVES) {
                            val isAlive = i <= livesRemaining
                            val heartColor by animateColorAsState(
                                targetValue = if (isAlive) LiteverTheme.colors.error else LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.25f),
                                label = "heartColor"
                            )
                            val heartScale by animateFloatAsState(
                                targetValue = if (isAlive) 1f else 0.8f,
                                label = "heartScale"
                            )
                            Icon(
                                imageVector = if (isAlive) ReMindIcons.Favorite else ReMindIcons.FavoriteBorder,
                                contentDescription = null,
                                tint = heartColor,
                                modifier = Modifier
                                    .size(20.dp)
                                    .scale(heartScale)
                            )
                        }
                    }
                }

                // Thanh tiến độ: đếm ngược khi Ghi nhớ, tiến độ tìm ô khi Đang chơi / Kết thúc
                val progressTarget = if (gameState == MemoryGameState.MEMORIZE) {
                    countdown / 3f
                } else {
                    (correctCount.toFloat() / currentBoard.targetTiles.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                }
                val progress by animateFloatAsState(
                    targetValue = progressTarget,
                    animationSpec = if (gameState == MemoryGameState.MEMORIZE) {
                        tween(1000, easing = FastOutSlowInEasing)
                    } else {
                        tween(250, easing = FastOutSlowInEasing)
                    },
                    label = "memoryProgress"
                )
                val progressColor by animateColorAsState(
                    targetValue = when (gameState) {
                        MemoryGameState.SUCCESS -> LiteverTheme.colors.success
                        MemoryGameState.FAILURE -> LiteverTheme.colors.error
                        else -> LiteverTheme.colors.primary
                    },
                    label = "memoryProgressColor"
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = progressColor,
                    trackColor = LiteverTheme.colors.surfaceContainerHighest
                )
            }
        }

        // 3. Khung chứa lưới ô nhớ (Interactive 3D Flip Grid)
        val boardBorderColor by animateColorAsState(
            targetValue = when (gameState) {
                MemoryGameState.SUCCESS -> LiteverTheme.colors.success
                MemoryGameState.FAILURE -> LiteverTheme.colors.error
                else -> LiteverTheme.colors.outlineVariant
            },
            label = "boardBorderColor"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = LiteverTheme.colors.surfaceContainerLowest
            ),
            shape = LiteverTheme.shapes.large,
            border = BorderStroke(1.5.dp, boardBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LiteverTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(currentBoard.gridSize),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small),
                    userScrollEnabled = false
                ) {
                    items(currentBoard.gridSize * currentBoard.gridSize) { index ->
                        val isTarget = currentBoard.targetIndices.contains(index)
                        val isSelected = selectedIndices.contains(index)

                        MemoryTileItem(
                            index = index,
                            isTarget = isTarget,
                            isSelected = isSelected,
                            gameState = gameState,
                            onTileClick = {
                                if (gameState == MemoryGameState.PLAYING && !isSelected) {
                                    onUserInteraction()
                                    selectedIndices = selectedIndices + index
                                    if (isTarget) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        val foundAll = currentBoard.targetIndices.all { selectedIndices.contains(it) || it == index }
                                        if (foundAll) {
                                            gameState = MemoryGameState.SUCCESS
                                        }
                                    } else {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        val newLives = livesRemaining - 1
                                        livesRemaining = newLives
                                        if (newLives <= 0) {
                                            gameState = MemoryGameState.FAILURE
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                // Chú thích phân loại ô khi ở trạng thái Thất bại (Reveal All)
                if (gameState == MemoryGameState.FAILURE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = LiteverTheme.spacing.small),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(
                            color = LiteverTheme.colors.success,
                            icon = ReMindIcons.Check,
                            text = stringResource(R.string.memory_legend_correct)
                        )
                        LegendItem(
                            color = LiteverTheme.colors.error,
                            icon = ReMindIcons.Close,
                            text = stringResource(R.string.memory_legend_wrong)
                        )
                        LegendItem(
                            color = LiteverTheme.colors.tertiary,
                            icon = ReMindIcons.Star,
                            text = stringResource(R.string.memory_legend_missed)
                        )
                    }
                }
            }
        }
        }

        // 4. Nút hành động riêng cho Trò chơi ô nhớ (giữ chiều cao ổn định tránh giật bố cục)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            when (gameState) {
                MemoryGameState.SUCCESS -> {
                    LvButton(
                        onClick = onContinue,
                        semantic = LvSemantic.Primary,
                        modifier = Modifier.fillMaxWidth(2f / 3f)
                    ) {
                        Text(
                            text = stringResource(R.string.action_continue),
                            style = LiteverTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(LiteverTheme.spacing.extraSmall))
                        Icon(
                            imageVector = ReMindIcons.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                MemoryGameState.FAILURE -> {
                    LvButton(
                        onClick = resetRound,
                        semantic = LvSemantic.Destructive,
                        modifier = Modifier.fillMaxWidth(2f / 3f)
                    ) {
                        Text(
                            text = stringResource(R.string.action_retry_now),
                            style = LiteverTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(LiteverTheme.spacing.extraSmall))
                        Icon(
                            imageVector = ReMindIcons.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}

/**
 * Từng ô nhớ riêng lẻ hỗ trợ Lật 3D, Lắc ngang khi chọn sai, và Nhún nhẹ khi nhấn.
 */
@Composable
private fun MemoryTileItem(
    index: Int,
    isTarget: Boolean,
    isSelected: Boolean,
    gameState: MemoryGameState,
    onTileClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Lắc ô khi chạm sai
    LaunchedEffect(isSelected) {
        if (isSelected && !isTarget && gameState == MemoryGameState.PLAYING) {
            coroutineScope.launch {
                shakeOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 350
                        -12f at 50
                        12f at 100
                        -8f at 150
                        8f at 200
                        -4f at 250
                        4f at 300
                        0f at 350
                    }
                )
            }
        }
    }

    // Trạng thái lật mở mặt trước (Revealed)
    val isRevealed = when (gameState) {
        MemoryGameState.MEMORIZE -> isTarget
        MemoryGameState.PLAYING -> isSelected
        MemoryGameState.SUCCESS -> isTarget
        MemoryGameState.FAILURE -> true // Reveal all
    }

    // Góc xoay 3D quanh trục Y: 0f (Úp) -> 180f (Mở)
    val rotationY by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "tileFlip"
    )

    // Hiệu ứng nhún khi ấn
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "tilePressScale"
    )

    // Xác định màu sắc và biểu tượng mặt sau/mặt trước
    val isFrontFace = rotationY > 90f
    val targetColor = when {
        !isRevealed -> LiteverTheme.colors.surfaceContainerHighest
        gameState == MemoryGameState.FAILURE && !isSelected && isTarget -> LiteverTheme.colors.tertiaryContainer
        gameState == MemoryGameState.FAILURE && !isSelected && !isTarget -> LiteverTheme.colors.surfaceContainerHighest.copy(alpha = 0.4f)
        isSelected && !isTarget -> LiteverTheme.colors.error
        isSelected && isTarget -> LiteverTheme.colors.success
        else -> LiteverTheme.colors.primary
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(250),
        label = "tileColor"
    )

    val isClickable = gameState == MemoryGameState.PLAYING && !isSelected

    Surface(
        onClick = onTileClick,
        enabled = isClickable,
        interactionSource = interactionSource,
        modifier = Modifier
            .aspectRatio(1f)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .scale(pressScale)
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 12f * density
            }
            .semantics {
                selected = isSelected
                stateDescription = if (isRevealed) "Revealed" else "Hidden"
            },
        shape = RoundedCornerShape(14.dp),
        color = animatedColor,
        border = if (gameState == MemoryGameState.FAILURE && !isSelected && isTarget) {
            BorderStroke(2.dp, LiteverTheme.colors.tertiary)
        } else null
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .graphicsLayer {
                    if (isFrontFace) {
                        this.rotationY = 180f // Lật lại nội dung icon để không bị ngược chiều
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (gameState == MemoryGameState.FAILURE && isRevealed && isFrontFace) {
                when {
                    isSelected && isTarget -> {
                        Icon(
                            imageVector = ReMindIcons.Check,
                            contentDescription = null,
                            tint = LiteverTheme.colors.onSuccess,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    isSelected && !isTarget -> {
                        Icon(
                            imageVector = ReMindIcons.Close,
                            contentDescription = null,
                            tint = LiteverTheme.colors.onError,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    isTarget -> {
                        Icon(
                            imageVector = ReMindIcons.Star,
                            contentDescription = null,
                            tint = LiteverTheme.colors.onTertiaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = text,
            style = LiteverTheme.typography.labelSmall,
            color = LiteverTheme.colors.onSurfaceVariant
        )
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
                onContinue = {}
            )
        }
    }
}
