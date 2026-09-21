package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.components.textfield.LvTextField
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.MathProblem
import vn.io.litever.remind.features.mission.R

/**
 * Giao diện nhiệm vụ Giải toán (Math Mission) theo chuẩn thiết kế Stitch:
 * - Phía trên: Banner hướng dẫn ngắn gọn (MissionInstructionBanner).
 * - Phía dưới: Card hiển thị phép toán lớn, sắc nét kèm ô nhập kết quả số ngay bên dưới.
 *   Loại bỏ badge tiến độ vòng lặp dư thừa.
 */
@Composable
fun MathMissionContent(
    problem: MathProblem?,
    currentRepetition: Int,
    totalRepetitions: Int,
    userInput: String,
    onUserInputChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
    ) {
        // 1. Phía trên: Banner hướng dẫn ngắn gọn
        MissionInstructionBanner(
            icon = Icons.Rounded.Calculate,
            requirementText = stringResource(R.string.mission_math_requirement)
        )

        // 2. Phía dưới: Vùng làm nhiệm vụ giải toán
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LiteverTheme.spacing.extraLarge, horizontal = LiteverTheme.spacing.large),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = problem?.expression ?: "",
                    style = LiteverTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = LiteverTheme.colors.onSurface
                )
            }
        }

        // Ô nhập kết quả số
        LvTextField(
            value = userInput,
            onValueChange = onUserInputChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = stringResource(R.string.mission_math_placeholder),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
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
                userInput = "5",
                onUserInputChange = {}
            )
        }
    }
}
