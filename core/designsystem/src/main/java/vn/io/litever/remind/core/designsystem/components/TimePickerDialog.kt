package vn.io.litever.remind.core.designsystem.components

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.components.dialog.LvAlertDialog
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.R

/**
 * ReMind Time Picker Dialog wrapping LiteVer [LvAlertDialog] with support for [androidx.compose.material3.TimePicker] or [androidx.compose.material3.TimeInput].
 *
 * @param onDismissRequest Called when user dismisses the dialog.
 * @param confirmButton Composable slot for the confirm button.
 * @param modifier The modifier for this dialog.
 * @param title Optional title for the dialog.
 * @param dismissButton Optional composable slot for dismiss button.
 * @param shape Shape of the dialog. Defaults to [LiteverTheme.shapes.extraLarge].
 * @param containerColor Background color.
 * @param tonalElevation Tonal elevation for the dialog.
 * @param properties Dialog properties (defaults to usePlatformDefaultWidth = false for optimal TimePicker display).
 * @param content The time picker composable content (e.g. TimePicker or TimeInput).
 */
@Composable
fun ReMindTimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = stringResource(R.string.select_time),
    dismissButton: @Composable (() -> Unit)? = null,
    shape: Shape = LiteverTheme.shapes.extraLarge,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable () -> Unit
) {
    LvAlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        title = title?.let {
            {
                Text(
                    text = it,
                    style = LiteverTheme.typography.headlineSmall,
                    color = LiteverTheme.colors.onSurface
                )
            }
        },
        text = content,
        shape = shape,
        containerColor = containerColor,
        properties = properties
    )
}

/**
 * Convenience overload for [ReMindTimePickerDialog] with text labels and click callbacks.
 */
@Composable
fun ReMindTimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButtonText: String,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = stringResource(R.string.select_time),
    dismissButtonText: String? = null,
    onDismissClick: (() -> Unit)? = null,
    shape: Shape = LiteverTheme.shapes.extraLarge,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable () -> Unit
) {
    ReMindTimePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            LvButton(
                onClick = onConfirmClick,
                semantic = LvSemantic.Primary
            ) {
                Text(text = confirmButtonText)
            }
        },
        modifier = modifier,
        title = title,
        dismissButton = dismissButtonText?.let {
            {
                LvButton(
                    onClick = onDismissClick ?: onDismissRequest,
                    type = LvButtonType.Outlined,
                    semantic = LvSemantic.Secondary
                ) {
                    Text(text = it)
                }
            }
        },
        shape = shape,
        containerColor = containerColor,
        tonalElevation = tonalElevation,
        properties = properties,
        content = content
    )
}

// Backward-compatibility alias for existing screens
@Composable
fun LiteverTimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = stringResource(R.string.select_time),
    dismissButton: @Composable (() -> Unit)? = null,
    shape: Shape = LiteverTheme.shapes.extraLarge,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable () -> Unit
) = ReMindTimePickerDialog(
    onDismissRequest = onDismissRequest,
    confirmButton = confirmButton,
    modifier = modifier,
    title = title,
    dismissButton = dismissButton,
    shape = shape,
    containerColor = containerColor,
    tonalElevation = tonalElevation,
    properties = properties,
    content = content
)
