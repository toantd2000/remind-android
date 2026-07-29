package vn.io.litever.remind.features.mission.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.components.LiteverScaffold
import vn.io.litever.designsystem.components.LiteverTopAppBar
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.model.MathProblem
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.features.mission.R
import vn.io.litever.remind.features.mission.ui.components.MathMissionContent
import vn.io.litever.remind.features.mission.ui.components.MissionCompleteContent
import vn.io.litever.remind.features.mission.ui.components.TypingMissionContent
import vn.io.litever.remind.features.mission.viewmodel.MissionRingingViewModel
import vn.io.litever.designsystem.components.LiteverLinearProgressIndicator

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
    uiState: vn.io.litever.remind.features.mission.viewmodel.MissionRingingUiState,
    userInput: String,
    onUserInputChange: (String) -> Unit,
    onFinish: () -> Unit,
    onAbandon: () -> Unit
) {
    BackHandler { onAbandon() } // Back button abandons mission

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(LiteverTheme.colors.background))
        return
    }

    LiteverScaffold(
        topBar = {
            if (!uiState.isMissionJustCompleted && !uiState.isDismissed) {
                LiteverTopAppBar(
                    title = stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_title),
                    onBackClick = onAbandon,
                    actions = {
                        Surface(
                            shape = LiteverTheme.shapes.small,
                            color = if (uiState.timeoutCountdown < 10)
                                LiteverTheme.colors.errorContainer 
                            else
                                LiteverTheme.colors.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "${uiState.timeoutCountdown}s",
                                style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (uiState.timeoutCountdown < 10)
                                    LiteverTheme.colors.error 
                                else
                                    LiteverTheme.colors.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                )
            }
        },
        bottomBar = {
            if (!uiState.isMissionJustCompleted && !uiState.isDismissed) {
                ReMindBottomBar {
                    LiteverButton(
                        onClick = onFinish,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = when (uiState.currentMission?.type) {
                            MissionType.TYPING -> userInput == (uiState.currentTargetData as? Phrase)?.content
                            else -> userInput.isNotBlank()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.mission_complete),
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
                    stringResource(R.string.mission_complete_next)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Global Mission Progress
                if (uiState.missions.size > 1) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        LiteverLinearProgressIndicator(
                            progress = { (uiState.currentMissionIndex + 1).toFloat() / uiState.missions.size },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(
                                R.string.mission_global_progress,
                                uiState.currentMissionIndex + 1,
                                uiState.missions.size
                            ),
                            style = LiteverTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                            color = LiteverTheme.colors.onSurfaceVariant
                        )
                    }
                }

                val currentMission = uiState.currentMission
                if (currentMission != null) {
                    when (currentMission.type) {
                        MissionType.TYPING -> {
                            TypingMissionContent(
                                targetPhrase = uiState.currentTargetData as? Phrase,
                                currentRepetition = uiState.currentRepetition,
                                totalRepetitions = currentMission.repeatCount,
                                userInput = userInput,
                                onUserInputChange = onUserInputChange
                            )
                        }
                        MissionType.MATH -> {
                            MathMissionContent(
                                problem = uiState.currentTargetData as? MathProblem,
                                currentRepetition = uiState.currentRepetition,
                                totalRepetitions = currentMission.repeatCount,
                                userInput = userInput,
                                onUserInputChange = onUserInputChange
                            )
                        }
                        else -> {
                            Text("Mission type ${currentMission.type} not implemented yet")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MissionRingingScreenPreview() {
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        MissionRingingScreen(
            uiState = vn.io.litever.remind.features.mission.viewmodel.MissionRingingUiState(
                isLoading = false,
                alarm = vn.io.litever.remind.core.model.Alarm(
                    id = 1,
                    time = java.time.LocalTime.of(7, 30),
                    label = "Wake up!"
                ),
                missions = listOf(
                    vn.io.litever.remind.core.model.Mission(
                        alarmId = 1,
                        type = MissionType.TYPING,
                        order = 0,
                        repeatCount = 3
                    )
                ),
                currentTargetData = Phrase(content = "Success is not final", categoryId = "motivation")
            ),
            userInput = "Succ",
            onUserInputChange = {},
            onFinish = {},
            onAbandon = {}
        )
    }
}










