package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.MathProblem
import vn.io.litever.remind.features.mission.R

private const val MAX_INPUT_DIGITS = 6

/**
 * Giao diện nhiệm vụ Giải toán (Math Mission) với Bàn phím số tích hợp (Built-in Keypad):
 * - Phía trên: Banner hướng dẫn ngắn gọn (MissionInstructionBanner).
 * - Ở giữa: Thẻ hiển thị phép toán lớn, sắc nét + Hàng hiển thị kết quả người dùng nhập.
 * - Phía dưới: Bàn phím số to, dễ thao tác ngay cả khi vừa tỉnh ngủ, có phản hồi rung (Haptic feedback)
 *   và các phím chức năng Xóa (Backspace) & Xác nhận (Check).
 */
@Composable
fun MathMissionContent(
    problem: MathProblem?,
    currentRepetition: Int,
    totalRepetitions: Int,
    userInput: String,
    onUserInputChange: (String) -> Unit,
    onSubmit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

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
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
    ) {
        // 1. Phía trên: Banner hướng dẫn ngắn gọn
        MissionInstructionBanner(
            icon = ReMindIcons.Calculate,
            requirementText = stringResource(R.string.mission_math_requirement)
        )

        // 2. Thẻ hiển thị phép tính và kết quả nhập
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = LiteverTheme.colors.surfaceContainerLowest
            ),
            shape = LiteverTheme.shapes.medium,
            border = BorderStroke(
                1.5.dp,
                LiteverTheme.colors.primary.copy(alpha = 0.25f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LiteverTheme.spacing.large, horizontal = LiteverTheme.spacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
            ) {
                // Biểu thức toán học
                Text(
                    text = problem?.expression ?: "",
                    style = LiteverTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = LiteverTheme.colors.onSurface
                )

                // Hàng hiển thị kết quả người dùng nhập
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = LiteverTheme.colors.surfaceContainerHighest.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(0.75f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = LiteverTheme.spacing.smallMedium, horizontal = LiteverTheme.spacing.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (userInput.isEmpty()) stringResource(R.string.mission_math_placeholder) else userInput,
                            style = LiteverTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (userInput.isEmpty()) {
                                LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                            } else {
                                LiteverTheme.colors.primary
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 3. Bàn phím số chuyên dụng (In-app Keypad)
        MathKeypad(
            onDigitClick = onDigitClick,
            onDeleteClick = onDeleteClick,
            onClearClick = onClearClick,
            onSubmitClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSubmit()
            },
            canSubmit = userInput.isNotBlank(),
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
        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
    ) {
        rows.forEach { rowDigits ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
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

        // Hàng cuối: [Xóa / Xóa hết] - [0] - [Xác nhận]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
        ) {
            // Nút xóa (Backspace)
            KeypadActionButton(
                icon = ReMindIcons.Backspace,
                contentDescription = stringResource(R.string.mission_math_delete),
                onClick = onDeleteClick,
                containerColor = LiteverTheme.colors.surfaceContainerHighest,
                contentColor = LiteverTheme.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            // Nút 0
            KeypadNumberButton(
                digit = '0',
                onClick = { onDigitClick('0') },
                modifier = Modifier.weight(1f)
            )

            // Nút xác nhận kết quả
            KeypadActionButton(
                icon = ReMindIcons.Check,
                contentDescription = stringResource(R.string.mission_math_submit),
                onClick = onSubmitClick,
                enabled = canSubmit,
                containerColor = if (canSubmit) LiteverTheme.colors.primary else LiteverTheme.colors.surfaceContainerHighest.copy(alpha = 0.5f),
                contentColor = if (canSubmit) LiteverTheme.colors.onPrimary else LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.38f),
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
    Surface(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = LiteverTheme.colors.surfaceContainerHigh
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = digit.toString(),
                style = LiteverTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = LiteverTheme.colors.onSurface
            )
        }
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
    Surface(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
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
