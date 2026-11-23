package vn.io.litever.remind.core.designsystem.components

import vn.io.litever.remind.core.designsystem.theme.spacing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoSilenceBottomSheet(
    currentMinutes: Int,
    onMinutesSelect: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    options: List<Int> = listOf(1, 3, 5, 10, 15, 30),
    sheetState: SheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        ReMindBottomSheetContent(
            title = stringResource(R.string.auto_silence_title)
        ) {
            AutoSilenceBottomSheetContent(
                currentMinutes = currentMinutes,
                onMinutesSelect = onMinutesSelect,
                options = options
            )
        }
    }
}

@Composable
fun AutoSilenceBottomSheetContent(
    currentMinutes: Int,
    onMinutesSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    options: List<Int> = listOf(1, 3, 5, 10, 15, 30)
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = MaterialTheme.spacing.medium)
    ) {
        Text(
            text = stringResource(R.string.auto_silence_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.small
            )
        )

        options.forEach { option ->
            val label = if (option == 0) {
                stringResource(R.string.auto_silence_never)
            } else {
                stringResource(R.string.minutes_unit, option)
            }

            ListItem(
                leadingContent = {
                    RadioButton(
                        selected = currentMinutes == option,
                        onClick = { onMinutesSelect(option) }
                    )
                },
                modifier = Modifier.clickable { onMinutesSelect(option) }
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AutoSilenceBottomSheetContentPreview() {
    ReMindTheme {
        AutoSilenceBottomSheetContent(
            currentMinutes = 5,
            onMinutesSelect = {}
        )
    }
}
