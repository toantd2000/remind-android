package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.components.dialog.LvAlertDialog
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.R

/**
 * ReMind Time Picker Dialog wrapping [LvAlertDialog] and [TimePicker],
 * guaranteeing [LocalContext] preservation via [CompositionLocalProvider].
 *
 * @param onDismissRequest Called when the user dismisses the dialog or cancels.
 * @param onConfirmClick Called when the user clicks confirm/save.
 * @param timePickerState State for the [TimePicker].
 * @param modifier Modifier for the dialog.
 * @param title Title string for the dialog.
 * @param confirmButtonText Text for the confirm button.
 * @param dismissButtonText Text for the cancel/dismiss button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReMindTimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
    timePickerState: TimePickerState,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.set_alarm_time),
    confirmButtonText: String = stringResource(R.string.save),
    dismissButtonText: String = stringResource(R.string.cancel)
) {
    ReMindTimePickerDialog(
        onDismissRequest = onDismissRequest,
        onConfirmClick = onConfirmClick,
        modifier = modifier,
        title = title,
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText
    ) {
        TimePicker(state = timePickerState)
    }
}

/**
 * Slot-based ReMind Time Picker Dialog wrapping [LvAlertDialog],
 * guaranteeing [LocalContext] preservation via [CompositionLocalProvider].
 */
@Composable
fun ReMindTimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.set_alarm_time),
    confirmButtonText: String = stringResource(R.string.save),
    dismissButtonText: String = stringResource(R.string.cancel),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    CompositionLocalProvider(LocalContext provides context) {
        LvAlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = LiteverTheme.spacing.medium)
                )
            },
            confirmButton = {
                LvButton(
                    onClick = onConfirmClick,
                    semantic = LvSemantic.Primary,
                    modifier = Modifier.padding(start = LiteverTheme.spacing.small)
                ) {
                    Text(text = confirmButtonText)
                }
            },
            dismissButton = {
                LvButton(
                    onClick = onDismissRequest,
                    type = LvButtonType.Outlined,
                    semantic = LvSemantic.Secondary
                ) {
                    Text(text = dismissButtonText)
                }
            },
            text = content,
            modifier = modifier
        )
    }
}
