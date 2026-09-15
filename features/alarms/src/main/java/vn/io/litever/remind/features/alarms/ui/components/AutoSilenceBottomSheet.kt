package vn.io.litever.remind.features.alarms.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.alarms.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoSilenceBottomSheet(
    currentMinutes: Int,
    onMinutesSelect: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = LiteverTheme.spacing.none,
        modifier = modifier
    ) {
        AutoSilenceBottomSheetContent(
            currentMinutes = currentMinutes,
            onMinutesSelect = onMinutesSelect
        )
    }
}

@Composable
fun AutoSilenceBottomSheetContent(
    currentMinutes: Int,
    onMinutesSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(1, 3, 5, 10, 30)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = LiteverTheme.spacing.extraLarge)
    ) {
        Text(
            text = stringResource(R.string.auto_silence_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(LiteverTheme.spacing.medium)
        )

        Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))

        options.forEach { option ->
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(R.string.minutes_unit, option),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                },
                leadingContent = {
                    RadioButton(
                        selected = currentMinutes == option,
                        onClick = { onMinutesSelect(option) }
                    )
                },
                modifier = Modifier.clickable { onMinutesSelect(option) }
            )
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
