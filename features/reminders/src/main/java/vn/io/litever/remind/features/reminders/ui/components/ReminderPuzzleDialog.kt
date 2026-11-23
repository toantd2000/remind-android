package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.reminders.R
import kotlin.random.Random

@Composable
fun ReminderPuzzleDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetCode = remember {
        val randomNum = Random.nextInt(10000, 99999)
        randomNum.toString()
    }
    var enteredCode by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.puzzle_dialog_title),
                style = LiteverTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LiteverTheme.spacing.extraSmall),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.puzzle_dialog_message),
                    style = LiteverTheme.typography.bodyMedium,
                    color = LiteverTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))

                // Target code display box
                Surface(
                    modifier = Modifier.clip(LiteverTheme.shapes.medium),
                    color = LiteverTheme.colors.primaryContainer.copy(alpha = 0.6f),
                    shape = LiteverTheme.shapes.medium
                ) {
                    Text(
                        text = targetCode,
                        style = LiteverTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 6.sp
                        ),
                        color = LiteverTheme.colors.primary,
                        modifier = Modifier.padding(
                            horizontal = LiteverTheme.spacing.large,
                            vertical = LiteverTheme.spacing.smallMedium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))

                OutlinedTextField(
                    value = enteredCode,
                    onValueChange = {
                        enteredCode = it.filter { char -> char.isDigit() }.take(targetCode.length)
                        showError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = LiteverTheme.shapes.medium,
                    label = { Text(stringResource(R.string.puzzle_input_hint)) },
                    placeholder = { Text(stringResource(R.string.puzzle_input_hint)) },
                    isError = showError,
                    supportingText = if (showError) {
                        { Text(stringResource(R.string.puzzle_incorrect)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (enteredCode == targetCode) {
                                onConfirmed()
                            } else {
                                showError = true
                            }
                        }
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredCode == targetCode) {
                        onConfirmed()
                    } else {
                        showError = true
                    }
                },
                enabled = enteredCode.isNotBlank()
            ) {
                Text(stringResource(R.string.puzzle_confirm_button))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ReminderPuzzleDialogPreview() {
    ReMindTheme {
        ReminderPuzzleDialog(
            onDismissRequest = {},
            onConfirmed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderPuzzleDialogDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderPuzzleDialog(
            onDismissRequest = {},
            onConfirmed = {}
        )
    }
}
