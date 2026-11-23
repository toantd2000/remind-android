package vn.io.litever.remind.core.designsystem.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.errorColors

/**
 * Standard Alert Dialog for ReMind using native Material 3 [AlertDialog] and theme tokens.
 */
@Composable
fun ReMindAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButtonText: String,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    text: String? = null,
    dismissButtonText: String? = null,
    onDismissClick: (() -> Unit)? = null,
    isDestructive: Boolean = false,
    shape: Shape = LiteverTheme.shapes.extraLarge,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
    content: @Composable (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = onConfirmClick,
                colors = if (isDestructive) ButtonDefaults.errorColors() else ButtonDefaults.buttonColors()
            ) {
                Text(text = confirmButtonText)
            }
        },
        modifier = modifier,
        dismissButton = dismissButtonText?.let {
            {
                OutlinedButton(
                    onClick = onDismissClick ?: onDismissRequest
                ) {
                    Text(text = it)
                }
            }
        },
        title = title?.let {
            {
                Text(
                    text = it,
                    style = LiteverTheme.typography.headlineSmall,
                    color = LiteverTheme.colors.onSurface
                )
            }
        },
        text = content ?: text?.let {
            {
                Text(
                    text = it,
                    style = LiteverTheme.typography.bodyMedium,
                    color = LiteverTheme.colors.onSurfaceVariant
                )
            }
        },
        shape = shape,
        containerColor = containerColor,
        tonalElevation = tonalElevation,
        properties = properties
    )
}

/**
 * Flexible Composable slot-based Dialog overload for ReMind using native Material 3 [AlertDialog].
 */
@Composable
fun ReMindAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = LiteverTheme.shapes.extraLarge,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties()
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = containerColor,
        tonalElevation = tonalElevation,
        properties = properties
    )
}
