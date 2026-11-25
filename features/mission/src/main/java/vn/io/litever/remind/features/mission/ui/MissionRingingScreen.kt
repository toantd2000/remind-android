package vn.io.litever.remind.features.mission.ui

import vn.io.litever.brand.tokens.LiteverTokens
import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.LocalAdManager
import vn.io.litever.remind.core.model.BarcodeMissionConfig
import vn.io.litever.remind.core.model.ShakeMissionConfig
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.material3.IconButton
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.MathProblem
import vn.io.litever.remind.core.model.MemoryGameBoard
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.features.mission.R
import vn.io.litever.remind.features.mission.ui.components.MathMissionContent
import vn.io.litever.remind.features.mission.ui.components.MemoryGameState
import vn.io.litever.remind.features.mission.ui.components.MemoryTilesMissionContent
import vn.io.litever.remind.features.mission.ui.components.MissionCompleteContent
import vn.io.litever.remind.features.mission.ui.components.TypingMissionContent
import vn.io.litever.remind.features.mission.viewmodel.MissionRingingUiState
import vn.io.litever.remind.features.mission.viewmodel.MissionRingingViewModel
import vn.io.litever.remind.features.mission.viewmodel.StepValidationState
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
        onUserInteraction = viewModel::onUserInteraction,
        onTimerPauseChange = viewModel::setTimerPaused,
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
    onUserInteraction: () -> Unit,
    onTimerPauseChange: (Boolean) -> Unit,
    onFinish: () -> Unit,
    onAbandon: () -> Unit
) {
    BackHandler { onAbandon() }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    val isUrgent = uiState.timeoutCountdown < 10
    val timerContainerColor by animateColorAsState(
        targetValue = if (isUrgent) MaterialTheme.colorScheme.error.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        label = "timerContainerColor"
    )
    val timerContentColor by animateColorAsState(
        targetValue = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        label = "timerContentColor"
    )

    val alarmTimeString = uiState.alarm?.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""

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
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (alarmTimeString.isNotBlank()) {
                                    Text(
                                        text = stringResource(R.string.alarm_time_subtitle, alarmTimeString),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onAbandon) {
                                Icon(
                                    imageVector = ReMindIcons.Close,
                                    contentDescription = stringResource(vn.io.litever.remind.core.designsystem.R.string.cancel)
                                )
                            }
                        },
                        actions = {
                            Surface(
                                shape = CircleShape,
                                color = timerContainerColor,
                                modifier = Modifier.padding(end = MaterialTheme.spacing.medium)
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = MaterialTheme.spacing.smallMedium,
                                        vertical = MaterialTheme.spacing.extraSmall
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                                ) {
                                    Icon(
                                        imageVector = ReMindIcons.Alarm,
                                        contentDescription = null,
                                        tint = timerContentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    val currentLocale = androidx.compose.ui.platform.LocalLocale.current.platformLocale
                                    Text(
                                        text = String.format(currentLocale, "%02d:%02d", uiState.timeoutCountdown / 60, uiState.timeoutCountdown % 60),
                                        style = LiteverTokens.typography.labelMediumEmphasized.copy(
                                            fontFeatureSettings = "tnum"
                                        ),
                                        color = timerContentColor
                                    )
                                }
                            }
                        }
                    )

                    if (!LocalInspectionMode.current) {
                        LocalAdManager.current.MicroAdStrip(
                            placement = AdPlacement.MISSION_TOP_STRIP,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Top Progress Scaffold (Tên nhiệm vụ + Số vòng + Segmented Progress Bar)
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
                                    horizontal = MaterialTheme.spacing.medium,
                                    vertical = MaterialTheme.spacing.smallMedium
                                )
                        )
                    }
                }
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
                    stringResource(R.string.mission_complete_next),
                completedStep = uiState.currentMissionIndex + 1,
                totalSteps = uiState.missions.size
            )
        } else {
            val currentMission = uiState.currentMission
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .padding(bottom = MaterialTheme.spacing.medium),
                contentAlignment = Alignment.TopCenter
            ) {
                if (currentMission != null) {
                    AnimatedContent(
                        targetState = Triple(uiState.currentMissionIndex, uiState.currentRepetition, currentMission.type),
                        transitionSpec = {
                            (slideInHorizontally(animationSpec = tween(250)) { it / 2 } + fadeIn(animationSpec = tween(250)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(250)) { -it / 2 } + fadeOut(animationSpec = tween(250)))
                        },
                        label = "missionContentTransition"
                    ) { (_, _, type) ->
                        when (type) {
                            MissionType.TYPING -> {
                                TypingMissionContent(
                                    targetPhrase = uiState.currentTargetData as? Phrase,
                                    userInput = userInput,
                                    onUserInputChange = onUserInputChange,
                                    onSubmit = onFinish,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            MissionType.MATH -> {
                                MathMissionContent(
                                    problem = uiState.currentTargetData as? MathProblem,
                                    currentRepetition = uiState.currentRepetition,
                                    totalRepetitions = currentMission.repeatCount,
                                    userInput = userInput,
                                    validationState = uiState.validationState,
                                    onUserInputChange = onUserInputChange,
                                    onSubmit = onFinish,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            MissionType.MEMORY_FIND_COLOR_TILES -> {
                                MemoryTilesMissionContent(
                                    board = uiState.currentTargetData as? MemoryGameBoard,
                                    onUserInteraction = onUserInteraction,
                                    onTimerPauseChange = onTimerPauseChange,
                                    onContinue = {
                                        onUserInputChange("SUCCESS")
                                        onFinish()
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            MissionType.SHAKE -> {
                                val config = (uiState.currentTargetData as? ShakeMissionConfig) ?: ShakeMissionConfig()
                                vn.io.litever.remind.features.mission.ui.components.ShakeMissionContent(
                                    targetCount = config.targetCount,
                                    sensitivity = config.sensitivity,
                                    onInteraction = onUserInteraction,
                                    onComplete = {
                                        onUserInputChange("SUCCESS")
                                        onFinish()
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            MissionType.BARCODE -> {
                                val config = (uiState.currentTargetData as? BarcodeMissionConfig) ?: BarcodeMissionConfig()
                                vn.io.litever.remind.features.mission.ui.components.BarcodeMissionContent(
                                    targetBarcode = config.targetBarcode,
                                    label = config.label,
                                    onInteraction = onUserInteraction,
                                    onComplete = {
                                        onUserInputChange("SUCCESS")
                                        onFinish()
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            MissionType.RANDOM -> {}
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
        MissionType.SHAKE -> stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_shake)
        MissionType.BARCODE -> stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_barcode)
        MissionType.RANDOM -> stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_random)
    }

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
        ) {
            // Header Row: [Nhiệm vụ 1/2] Gõ chữ ------- [flag] Vòng 2/3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    if (totalMissions > 1) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.mission_step_badge,
                                    currentMissionIndex + 1,
                                    totalMissions
                                ),
                                style = LiteverTokens.typography.labelSmallEmphasized,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(
                                    horizontal = MaterialTheme.spacing.small,
                                    vertical = MaterialTheme.spacing.tiny
                                )
                            )
                        }
                    }
                    Text(
                        text = missionName,
                        style = LiteverTokens.typography.labelMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Vòng x/y
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                ) {
                    Icon(
                        imageVector = ReMindIcons.Flag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(
                            R.string.mission_round_badge,
                            currentRepetition,
                            totalRepetitions
                        ),
                        style = LiteverTokens.typography.labelSmallEmphasized,
                        color = MaterialTheme.colorScheme.primary
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
                    val targetAlpha = when {
                        isFinished -> 1f
                        isCurrent -> 0.7f
                        else -> 0.15f
                    }
                    val animatedAlpha by animateFloatAsState(
                        targetValue = targetAlpha,
                        animationSpec = tween(300),
                        label = "segmentAlpha"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = animatedAlpha))
                    )
                }
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
            onUserInteraction = {},
            onTimerPauseChange = {},
            onFinish = {},
            onAbandon = {}
        )
    }
}
