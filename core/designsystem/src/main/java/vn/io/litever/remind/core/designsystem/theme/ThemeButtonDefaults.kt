package vn.io.litever.remind.core.designsystem.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// ==========================================
// SEMANTIC BUTTON & ICONBUTTON COLOR PRESETS
// ==========================================

@Composable
fun ButtonDefaults.errorColors(): ButtonColors = buttonColors(
    containerColor = MaterialTheme.colorScheme.error,
    contentColor = MaterialTheme.colorScheme.onError
)

@Composable
fun ButtonDefaults.errorTonalColors(): ButtonColors = filledTonalButtonColors(
    containerColor = MaterialTheme.colorScheme.errorContainer,
    contentColor = MaterialTheme.colorScheme.onErrorContainer
)

@Composable
fun ButtonDefaults.errorTextColors(): ButtonColors = textButtonColors(
    contentColor = MaterialTheme.colorScheme.error
)

@Composable
fun ButtonDefaults.warningColors(): ButtonColors = buttonColors(
    containerColor = MaterialTheme.colorScheme.warning,
    contentColor = MaterialTheme.colorScheme.onWarning
)

@Composable
fun ButtonDefaults.warningTonalColors(): ButtonColors = filledTonalButtonColors(
    containerColor = MaterialTheme.colorScheme.warningContainer,
    contentColor = MaterialTheme.colorScheme.onWarningContainer
)

@Composable
fun ButtonDefaults.successColors(): ButtonColors = buttonColors(
    containerColor = MaterialTheme.colorScheme.success,
    contentColor = MaterialTheme.colorScheme.onSuccess
)

@Composable
fun ButtonDefaults.successTonalColors(): ButtonColors = filledTonalButtonColors(
    containerColor = MaterialTheme.colorScheme.successContainer,
    contentColor = MaterialTheme.colorScheme.onSuccessContainer
)

@Composable
fun IconButtonDefaults.errorColors(): IconButtonColors = iconButtonColors(
    contentColor = MaterialTheme.colorScheme.error
)

@Composable
fun IconButtonDefaults.warningColors(): IconButtonColors = iconButtonColors(
    contentColor = MaterialTheme.colorScheme.warning
)

@Composable
fun IconButtonDefaults.successColors(): IconButtonColors = iconButtonColors(
    contentColor = MaterialTheme.colorScheme.success
)
