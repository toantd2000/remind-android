package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Phrase

@Composable
fun TypingMissionContent(
    targetPhrase: Phrase?,
    currentRepetition: Int,
    totalRepetitions: Int,
    userInput: String,
    onUserInputChange: (String) -> Unit
) {
    val targetContent = targetPhrase?.content ?: ""
    
    // Define Premium Success Green (adapted for Light/Dark)
    val successColor = Color(0xFF4CAF50) // Material Green 500
    val errorColor = MaterialTheme.colorScheme.error
    
    // Advanced feedback logic: Green for correct, Red for first error
    val annotatedPhrase = buildAnnotatedString {
        var firstErrorIndex = -1
        for (i in userInput.indices) {
            if (i < targetContent.length) {
                if (userInput[i].lowercaseChar() != targetContent[i].lowercaseChar()) {
                    firstErrorIndex = i
                    break
                }
            } else {
                firstErrorIndex = targetContent.length
                break
            }
        }
        
        val lastCorrectIndex = if (firstErrorIndex == -1) userInput.length else firstErrorIndex

        // Correct part in Success Green
        withStyle(style = SpanStyle(color = successColor)) {
            append(targetContent.substring(0, lastCorrectIndex.coerceAtMost(targetContent.length)))
        }
        
        // Error character in Error Red
        if (firstErrorIndex != -1 && firstErrorIndex < targetContent.length) {
            withStyle(style = SpanStyle(color = errorColor, fontWeight = FontWeight.Bold)) {
                append(targetContent[firstErrorIndex])
            }
            // Remaining part in default dimmed
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))) {
                append(targetContent.substring(firstErrorIndex + 1))
            }
        } else if (lastCorrectIndex < targetContent.length) {
            // Remaining part in default dimmed
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))) {
                append(targetContent.substring(lastCorrectIndex))
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.mission_typing_instruction),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            shape = MaterialTheme.shapes.medium,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = annotatedPhrase,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        lineHeight = 32.sp
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.mission_progress, currentRepetition, totalRepetitions),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        
        OutlinedTextField(
            value = userInput,
            onValueChange = onUserInputChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { 
                Text(
                    stringResource(R.string.mission_phrase_placeholder),
                    style = MaterialTheme.typography.bodyLarge
                ) 
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (userInput.isNotEmpty() && !targetContent.startsWith(userInput, ignoreCase = true)) 
                    errorColor else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TypingMissionContentPreview() {
    ReMindTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TypingMissionContent(
                targetPhrase = Phrase(id = 1, content = "Success is not final, failure is not fatal.", categoryId = "motivation"),
                currentRepetition = 1,
                totalRepetitions = 3,
                userInput = "Succesx",
                onUserInputChange = {}
            )
        }
    }
}
