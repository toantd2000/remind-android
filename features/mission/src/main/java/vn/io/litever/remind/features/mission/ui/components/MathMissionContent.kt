package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.components.LiteVerTextFieldDefaults
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.MathProblem

@Composable
fun MathMissionContent(
    problem: MathProblem?,
    currentRepetition: Int,
    totalRepetitions: Int,
    userInput: String,
    onUserInputChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.mission_math_instruction),
            style = LiteverTheme.typography.titleSmall,
            color = LiteverTheme.colors.primary,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(LiteverTheme.spacing.smallMedium))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = LiteverTheme.shapes.medium,
            border = BorderStroke(
                1.dp,
                LiteverTheme.colors.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LiteverTheme.spacing.large),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = problem?.expression ?: "",
                    style = LiteverTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = LiteverTheme.colors.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))
        
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
        
        OutlinedTextField(
            value = userInput,
            onValueChange = onUserInputChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { 
                Text(
                    stringResource(R.string.mission_math_placeholder),
                    style = LiteverTheme.typography.bodyLarge
                ) 
            },
            singleLine = true,
            shape = LiteVerTextFieldDefaults.shape,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = LiteVerTextFieldDefaults.outlinedColors(),
            textStyle = LiteverTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MathMissionContentPreview() {
    ReMindTheme {
        Box(modifier = Modifier.padding(LiteverTheme.spacing.medium)) {
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










