package vn.io.litever.designsystem.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A standard Alert Dialog for the Litever application.
 * Wraps Material 3 AlertDialog with the app's design tokens.
 */
@Composable
fun LiteverAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButtonText: String,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    text: String? = null,
    dismissButtonText: String? = null,
    onDismissClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = title?.let { 
            { 
                Text(
                    text = it, 
                    style = MaterialTheme.typography.headlineSmall 
                ) 
            } 
        },
        text = content ?: text?.let { 
            { 
                Text(
                    text = it, 
                    style = MaterialTheme.typography.bodyMedium 
                ) 
            } 
        },
        confirmButton = {
            Button(onClick = onConfirmClick) {
                Text(text = confirmButtonText)
            }
        },
        dismissButton = dismissButtonText?.let {
            {
                TextButton(onClick = onDismissClick ?: onDismissRequest) {
                    Text(text = it)
                }
            }
        },
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
