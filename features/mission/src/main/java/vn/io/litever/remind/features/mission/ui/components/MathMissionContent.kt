package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.core.designsystem.theme.semanticColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.MathProblem
import vn.io.litever.remind.features.mission.R
import vn.io.litever.remind.features.mission.viewmodel.StepValidationState
import kotlin.math.roundToInt

private const val MAX_INPUT_DIGITS = 6

/**
 * Giao diện nhiệm vụ Giải toán (Math Mission) chuẩn Gamification:
 * - Hiệu ứng rung và lắc ngang khi nhập kết quả sai + báo lỗi đỏ + tự xóa sau 600ms.
 * - Hiệu ứng chuyển màu xanh lá + icon ✓ khi nhập kết quả đúng trước khi qua câu tiếp theo.
 * - Phản hồi rung nhún phím bấm trên bàn phím số tích hợp.
 */
@Composable
fun MathMissionContent(
    problem: MathProblem?,
    currentRepetition: Int,
    totalRepetitions: Int,
    userInput: String,
    validationState: StepValidationState = StepValidationState.IDLE,
    onUserInputChange: (String) -> Unit,
    onSubmit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(validationState) {
        if (validationState == StepValidationState.WRONG) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            coroutineScope.launch {
                shakeOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 350
                        -18f at 50
                        18f at 100
                        -12f at 150
                        12f at 200
                        -6f at 250
                        6f at 300
                        0f at 350
                    }
                )
            }
        } else if (validationState == StepValidationState.CORRECT) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val onDigitClick: (Char) -> Unit = { digit ->
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (userInput.length < MAX_INPUT_DIGITS) {
            val nextInput = if (userInput == "0") digit.toString() else userInput + digit
            onUserInputChange(nextInput)
        }
    }

    val onDeleteClick: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (userInput.isNotEmpty()) {
            onUserInputChange(userInput.dropLast(1))
        }
    }

    val onClearClick: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onUserInputChange("")
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            // 1. Phía trên: Banner hướng dẫn
            MissionInstructionBanner(
                icon = ReMindIcons.Calculate,
                requirementText = stringResource(R.string.mission_math_requirement)
            )

            // 2. Thẻ hiển thị phép tính và kết quả nhập
            val cardBorderColor by animateColorAsState(
                targetValue = when (validationState) {
                    StepValidationState.CORRECT -> MaterialTheme.semanticColors.success
                    StepValidationState.WRONG -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                },
                label = "cardBorderColor"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.5.dp, cardBorderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.large, horizontal = MaterialTheme.spacing.medium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                ) {
                    // Biểu thức toán học
                    Text(
                        text = problem?.expression ?: "",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Hàng hiển thị kết quả người dùng nhập
                    val surfaceBgColor by animateColorAsState(
                        targetValue = when (validationState) {
                            StepValidationState.CORRECT -> MaterialTheme.semanticColors.success.copy(alpha = 0.15f)
                            StepValidationState.WRONG -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                        },
                        label = "surfaceBgColor"
                    )

                    val resultTextColor by animateColorAsState(
                        targetValue = when (validationState) {
                            StepValidationState.CORRECT -> MaterialTheme.semanticColors.success
                            StepValidationState.WRONG -> MaterialTheme.colorScheme.error
                            else -> if (userInput.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
                        },
                        label = "resultTextColor"
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceBgColor,
                        modifier = Modifier.fillMaxWidth(0.75f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = MaterialTheme.spacing.smallMedium, horizontal = MaterialTheme.spacing.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                            ) {
                                Text(
                                    text = if (userInput.isEmpty()) stringResource(R.string.mission_math_placeholder) else userInput,
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                    color = resultTextColor,
                                    textAlign = TextAlign.Center
                                )
                                if (validationState == StepValidationState.CORRECT) {
                                    Icon(
                                        imageVector = ReMindIcons.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.semanticColors.success,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Dòng cảnh báo khi nhập sai
                    AnimatedVisibility(visible = validationState == StepValidationState.WRONG) {
                        Text(
                            text = stringResource(R.string.mission_math_wrong),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 3. Bàn phím số chuyên dụng (In-app Keypad) sát bên dưới
        MathKeypad(
            onDigitClick = onDigitClick,
            onDeleteClick = onDeleteClick,
            onClearClick = onClearClick,
            onSubmitClick = {
                onSubmit()
            },
            canSubmit = userInput.isNotBlank() && validationState == StepValidationState.IDLE,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MathKeypad(
    onDigitClick: (Char) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit,
    onSubmitClick: () -> Unit,
    canSubmit: Boolean,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9')
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        rows.forEach { rowDigits ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                rowDigits.forEach { digit ->
                    KeypadNumberButton(
                        digit = digit,
                        onClick = { onDigitClick(digit) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Hàng cuối: [Xóa] - [0] - [Xác nhận]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            KeypadActionButton(
                icon = ReMindIcons.Backspace,
                contentDescription = stringResource(R.string.mission_math_delete),
                onClick = onDeleteClick,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            KeypadNumberButton(
                digit = '0',
                onClick = { onDigitClick('0') },
                modifier = Modifier.weight(1f)
            )

            KeypadActionButton(
                icon = ReMindIcons.Check,
                contentDescription = stringResource(R.string.mission_math_submit),
                onClick = onSubmitClick,
                enabled = canSubmit,
                containerColor = if (canSubmit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                contentColor = if (canSubmit) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KeypadNumberButton(
    digit: Char,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    KeypadButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    ) {
        Text(
            text = digit.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun KeypadActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    KeypadButton(
        onClick = onClick,
        containerColor = containerColor,
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun KeypadButton(
    onClick: () -> Unit,
    containerColor: Color,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = tween(durationMillis = 80),
        label = "keypadBtnScale"
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .height(58.dp)
            .scale(scale),
        shape = RoundedCornerShape(18.dp),
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MathMissionContentPreview() {
    ReMindTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MathMissionContent(
                problem = MathProblem("12 + 45", 57),
                currentRepetition = 1,
                totalRepetitions = 3,
                userInput = "57",
                onUserInputChange = {},
                onSubmit = {}
            )
        }
    }
}
