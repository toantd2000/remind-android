package vn.io.litever.remind.features.mission.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import vn.io.litever.designsystem.theme.LiteverTheme
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
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    
    val successColor = LiteverTheme.colors.success
    val errorColor = LiteverTheme.colors.error
    val dimmedColor = LiteverTheme.colors.onSurface.copy(alpha = 0.2f)

    val visualTransformation = remember(targetContent, successColor, errorColor, dimmedColor) {
        VisualTransformation { text ->
            val input = text.text
            val annotatedString = buildAnnotatedString {
                // Handle typed characters (correct, wrong, or extra)
                for (i in input.indices) {
                    if (i < targetContent.length) {
                        if (input[i] == targetContent[i]) {
                            withStyle(style = SpanStyle(color = successColor)) {
                                append(targetContent[i])
                            }
                        } else {
                            withStyle(style = SpanStyle(color = errorColor, fontWeight = FontWeight.Bold)) {
                                append(input[i])
                            }
                        }
                    } else {
                        // Extra characters typed beyond target length
                        withStyle(style = SpanStyle(color = errorColor, fontWeight = FontWeight.Bold)) {
                            append(input[i])
                        }
                    }
                }
                
                // Handle remaining target characters as hint
                if (input.length < targetContent.length) {
                    withStyle(style = SpanStyle(color = dimmedColor)) {
                        append(targetContent.substring(input.length))
                    }
                }
            }
            
            TransformedText(
                text = annotatedString,
                offsetMapping = object : OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int = offset
                    override fun transformedToOriginal(offset: Int): Int = offset.coerceAtMost(input.length)
                }
            )
        }
    }

    val textStyle = LiteverTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        lineHeight = 32.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.mission_typing_instruction),
            style = LiteverTheme.typography.titleSmall,
            color = LiteverTheme.colors.primary,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(LiteverTheme.spacing.smallMedium))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { focusRequester.requestFocus() }
                ),
            colors = CardDefaults.cardColors(
                containerColor = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.2f)
            ),
            shape = LiteverTheme.shapes.medium,
            border = BorderStroke(
                1.dp,
                LiteverTheme.colors.outlineVariant.copy(alpha = 0.3f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LiteverTheme.spacing.large),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = userInput,
                    onValueChange = onUserInputChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = textStyle,
                    cursorBrush = SolidColor(LiteverTheme.colors.primary),
                    visualTransformation = visualTransformation,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    singleLine = false
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
    }
}

@Preview(showBackground = true)
@Composable
fun TypingMissionContentPreview() {
    ReMindTheme {
        Box(modifier = Modifier.padding(LiteverTheme.spacing.medium)) {
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










