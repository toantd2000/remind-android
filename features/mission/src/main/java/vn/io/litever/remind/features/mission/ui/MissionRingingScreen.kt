package vn.io.litever.remind.features.mission.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvIconButton
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.MathProblem
import vn.io.litever.remind.core.model.MemoryGameBoard
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.features.mission.R
import vn.io.litever.remind.features.mission.ui.components.MathMissionContent
import vn.io.litever.remind.features.mission.ui.components.MemoryTilesMissionContent
import vn.io.litever.remind.features.mission.ui.components.MissionCompleteContent
import vn.io.litever.remind.features.mission.ui.components.TypingMissionContent
import vn.io.litever.remind.features.mission.viewmodel.MissionRingingUiState
import vn.io.litever.remind.features.mission.viewmodel.MissionRingingViewModel
import java.time.format.DateTimeFormatter

@Composable
fun MissionRingingRoute(
    onFinish: () -> Unit,
    onAbandon: () -> Unit,
    navController: androidx.navigation.NavController,
    viewModel: MissionRingingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userInput by viewModel.userInput.collectAsState()

    LaunchedEffect(uiState.isDismissed) {
        if (uiState.isDismissed) {
            navController.previousBackStackEntry?.savedStateHandle?.set("mission_result", "success")
            onFinish()
        }
    }

    LaunchedEffect(uiState.isAbandoned) {
        if (uiState.isAbandoned) {
            navController.previousBackStackEntry?.savedStateHandle?.set("mission_result", "abandoned")
            onAbandon()
        }
    }

    MissionRingingScreen(
        uiState = uiState,
        userInput = userInput,
        onUserInputChange = viewModel::onUserInputChange,
        onFinish = viewModel::validateCurrentStep,
        onAbandon = {
            viewModel.abandonMission()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionRingingScreen(
    uiState: MissionRingingUiState,
    userInput: String,
    onUserInputChange: (String) -> Unit,
    onFinish: () -> Unit,
    onAbandon: () -> Unit
) {
    BackHandler { onAbandon() }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(LiteverTheme.colors.background))
        return
    }

    val isUrgent = uiState.timeoutCountdown < 10
    val timerContainerColor by animateColorAsState(
        targetValue = if (isUrgent) LiteverTheme.colors.error.copy(alpha = 0.12f) else LiteverTheme.colors.primary.copy(alpha = 0.08f),
        label = "timerContainerColor"
    )
    val timerContentColor by animateColorAsState(
        targetValue = if (isUrgent) LiteverTheme.colors.error else LiteverTheme.colors.primary,
        label = "timerContentColor"
    )

    val alarmTimeString = uiState.alarm?.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""
    var memoryCorrectTiles by remember(uiState.currentTargetData) { mutableStateOf(0) }
    var memoryTargetTiles by remember(uiState.currentTargetData) {
        mutableStateOf((uiState.currentTargetData as? MemoryGameBoard)?.targetTiles ?: 0)
    }

    Scaffold(
        topBar = {
            if (!uiState.isMissionJustCompleted && !uiState.isDismissed) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LvTopAppBar(
                        title = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.wakeup_challenge_title),
                                    style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = LiteverTheme.colors.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (alarmTimeString.isNotBlank()) {
                                    Text(
                                        text = stringResource(R.string.alarm_time_subtitle, alarmTimeString),
                                        style = LiteverTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = LiteverTheme.colors.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            LvIconButton(
                                onClick = onAbandon,
                                semantic = LvSemantic.Neutral
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(vn.io.litever.remind.core.designsystem.R.string.cancel)
                                )
                            }
                        },
                        actions = {
                            Surface(
                                shape = CircleShape,
                                color = timerContainerColor,
                                modifier = Modifier.padding(end = LiteverTheme.spacing.medium)
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = LiteverTheme.spacing.smallMedium,
                                        vertical = LiteverTheme.spacing.extraSmall
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.extraSmall)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Alarm,
                                        contentDescription = null,
                                        tint = timerContentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = String.format("%02d:%02d", uiState.timeoutCountdown / 60, uiState.timeoutCountdown % 60),
                                        style = LiteverTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = timerContentColor
                                    )
                                }
                            }
                        }
                    )

                    // Top Progress Scaffold (Tên nhiệm vụ + Số vòng + Segmented Progress Bar) ngay dưới LvTopAppBar
                    val currentMission = uiState.currentMission
                    if (currentMission != null) {
                        MissionProgressScaffold(
                            currentMissionType = currentMission.type,
                            currentMissionIndex = uiState.currentMissionIndex,
                            totalMissions = uiState.missions.size,
                            currentRepetition = uiState.currentRepetition,
                            totalRepetitions = currentMission.repeatCount,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = LiteverTheme.spacing.medium,
                                    vertical = LiteverTheme.spacing.smallMedium
                                )
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!uiState.isMissionJustCompleted && !uiState.isDismissed) {
                MissionBottomDockedBar(
                    userInput = userInput,
                    targetPhrase = uiState.currentTargetData as? Phrase,
                    currentMissionType = uiState.currentMission?.type,
                    memoryCorrectTiles = memoryCorrectTiles,
                    memoryTargetTiles = memoryTargetTiles,
                    onFinish = onFinish
                )
            }
        }
    ) { padding ->
        if (uiState.isMissionJustCompleted) {
            val isLastMission = uiState.currentMissionIndex >= uiState.missions.size - 1
            MissionCompleteContent(
                modifier = Modifier.padding(padding),
                subtitle = if (isLastMission)
                    stringResource(R.string.mission_complete_subtitle)
                else
                    stringResource(R.string.mission_complete_next)
            )
        } else {
            val currentMission = uiState.currentMission
            // Vùng chứa nội dung nhiệm vụ chiếm trọn không gian phía trên docked bar/IME, có khoảng đệm hai bên và đáy
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = LiteverTheme.spacing.medium)
                    .padding(bottom = LiteverTheme.spacing.medium),
                contentAlignment = Alignment.TopCenter
            ) {
                if (currentMission != null) {
                    when (currentMission.type) {
                        MissionType.TYPING -> {
                            TypingMissionContent(
                                targetPhrase = uiState.currentTargetData as? Phrase,
                                userInput = userInput,
                                onUserInputChange = onUserInputChange,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        MissionType.MATH -> {
                            MathMissionContent(
                                problem = uiState.currentTargetData as? MathProblem,
                                currentRepetition = uiState.currentRepetition,
                                totalRepetitions = currentMission.repeatCount,
                                userInput = userInput,
                                onUserInputChange = onUserInputChange,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                        MissionType.MEMORY_FIND_COLOR_TILES -> {
                            MemoryTilesMissionContent(
                                board = uiState.currentTargetData as? MemoryGameBoard,
                                onProgressChange = { correct, total ->
                                    memoryCorrectTiles = correct
                                    memoryTargetTiles = total
                                },
                                onSuccess = {
                                    onUserInputChange("SUCCESS")
                                    onFinish()
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                            else -> {
                                Text(
                                    text = "Mission type ${currentMission.type} not implemented yet",
                                    style = LiteverTheme.typography.bodyLarge,
                                    color = LiteverTheme.colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

/**
 * Khối hiển thị tiến độ nhiệm vụ và phân đoạn vòng lặp theo thiết kế Stitch.
 */
@Composable
private fun MissionProgressScaffold(
    currentMissionType: MissionType,
    currentMissionIndex: Int,
    totalMissions: Int,
    currentRepetition: Int,
    totalRepetitions: Int,
    modifier: Modifier = Modifier
) {
    val missionName = when (currentMissionType) {
        MissionType.TYPING -> stringResource(R.string.mission_typing)
        MissionType.MATH -> stringResource(R.string.mission_math)
        MissionType.MEMORY_FIND_COLOR_TILES -> stringResource(R.string.memory_game_config_title)
        else -> currentMissionType.name
    }

    Card(
        modifier = modifier,
        shape = LiteverTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = LiteverTheme.colors.surfaceContainer
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LiteverTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.smallMedium)
        ) {
            // Header Row: [Nhiệm vụ 1/2] Gõ chữ ------- [flag] Vòng 2/3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                ) {
                    if (totalMissions > 1) {
                        Surface(
                            shape = LiteverTheme.shapes.small,
                            color = LiteverTheme.colors.primary
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.mission_step_badge,
                                    currentMissionIndex + 1,
                                    totalMissions
                                ),
                                style = LiteverTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LiteverTheme.colors.onPrimary,
                                modifier = Modifier.padding(
                                    horizontal = LiteverTheme.spacing.small,
                                    vertical = 2.dp
                                )
                            )
                        }
                    }
                    Text(
                        text = missionName,
                        style = LiteverTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = LiteverTheme.colors.onSurface
                    )
                }

                // Vòng x/y
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Flag,
                        contentDescription = null,
                        tint = LiteverTheme.colors.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(
                            R.string.mission_round_badge,
                            currentRepetition,
                            totalRepetitions
                        ),
                        style = LiteverTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LiteverTheme.colors.primary
                    )
                }
            }

            // Segmented Progress Bar (Các đoạn phân khúc tương ứng số vòng)
            val segmentsCount = totalRepetitions.coerceAtLeast(1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 1..segmentsCount) {
                    val isFinished = i < currentRepetition
                    val isCurrent = i == currentRepetition
                    val segmentColor = when {
                        isFinished -> LiteverTheme.colors.primary
                        isCurrent -> LiteverTheme.colors.primary.copy(alpha = 0.7f)
                        else -> LiteverTheme.colors.surfaceContainerHighest
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(segmentColor)
                    )
                }
            }
        }
    }
}

/**
 * Docked Action Bar nằm sát dưới cùng (hoặc sát trên bàn phím ảo IME).
 */
@Composable
private fun MissionBottomDockedBar(
    userInput: String,
    targetPhrase: Phrase?,
    currentMissionType: MissionType?,
    memoryCorrectTiles: Int = 0,
    memoryTargetTiles: Int = 0,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = when (currentMissionType) {
        MissionType.TYPING -> userInput == targetPhrase?.content
        MissionType.MEMORY_FIND_COLOR_TILES -> userInput == "SUCCESS"
        else -> userInput.isNotBlank()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
        color = LiteverTheme.colors.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                    )
                )
                .padding(
                    horizontal = LiteverTheme.spacing.medium,
                    vertical = LiteverTheme.spacing.small
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Khu vực thông tin phụ bên trái (số ký tự cho Typing hoặc số ô cho Memory Tiles)
            when (currentMissionType) {
                MissionType.TYPING if targetPhrase != null -> {
                    val totalChars = targetPhrase.content.length
                    val typedChars = userInput.length.coerceAtMost(totalChars)
                    Text(
                        text = "$typedChars / $totalChars",
                        style = LiteverTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = LiteverTheme.colors.onSurfaceVariant
                    )
                }
                MissionType.MEMORY_FIND_COLOR_TILES if memoryTargetTiles > 0 -> {
                    Text(
                        text = stringResource(
                            R.string.mission_memory_tiles_progress,
                            memoryCorrectTiles,
                            memoryTargetTiles
                        ),
                        style = LiteverTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = LiteverTheme.colors.onSurfaceVariant
                    )
                }
                else -> {
                    Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))
                }
            }

            // Nút hành động chính bên phải
            LvButton(
                onClick = onFinish,
                enabled = isEnabled,
                semantic = LvSemantic.Primary
            ) {
                Text(
                    text = stringResource(R.string.action_continue),
                    style = LiteverTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(LiteverTheme.spacing.extraSmall))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MissionRingingScreenPreview() {
    ReMindTheme {
        MissionRingingScreen(
            uiState = MissionRingingUiState(
                isLoading = false,
                alarm = vn.io.litever.remind.core.model.Alarm(
                    id = 1,
                    time = java.time.LocalTime.of(6, 0),
                    label = "Wake up!"
                ),
                missions = listOf(
                    vn.io.litever.remind.core.model.Mission(
                        alarmId = 1,
                        type = MissionType.TYPING,
                        order = 0,
                        repeatCount = 3
                    ),
                    vn.io.litever.remind.core.model.Mission(
                        alarmId = 1,
                        type = MissionType.MATH,
                        order = 1,
                        repeatCount = 2
                    )
                ),
                currentMissionIndex = 0,
                currentRepetition = 2,
                currentTargetData = Phrase(content = "Dậy sớm để dẫn đầu", categoryId = "motivation")
            ),
            userInput = "Dậy sớm",
            onUserInputChange = {},
            onFinish = {},
            onAbandon = {}
        )
    }
}
