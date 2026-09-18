package vn.io.litever.remind.features.alarms.ui.components

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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindBottomSheetContent
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.alarms.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GentleAlarmBottomSheet(
    currentDuration: Int,
    onDurationSelect: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = LiteverTheme.colors.surface,
        modifier = modifier
    ) {
        ReMindBottomSheetContent(
            title = stringResource(R.string.gentle_alarm_title)
        ) {
            GentleAlarmBottomSheetContent(
                currentDuration = currentDuration,
                onDurationSelect = onDurationSelect
            )
        }
    }
}

@Composable
fun GentleAlarmBottomSheetContent(
    currentDuration: Int,
    onDurationSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(0, 5, 10, 20)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = LiteverTheme.spacing.medium)
    ) {
        Text(
            text = stringResource(R.string.gentle_alarm_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = LiteverTheme.spacing.medium, vertical = LiteverTheme.spacing.small)
        )

        options.forEach { option ->
            val label = if (option == 0) {
                stringResource(R.string.off)
            } else if (option < 60) {
                stringResource(R.string.seconds_unit, option)
            } else {
                stringResource(R.string.minutes_unit, option / 60)
            }

            ListItem(
                headlineContent = {
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                },
                leadingContent = {
                    RadioButton(
                        selected = currentDuration == option,
                        onClick = { onDurationSelect(option) }
                    )
                },
                modifier = Modifier.clickable { onDurationSelect(option) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GentleAlarmBottomSheetContentPreview() {
    ReMindTheme {
        GentleAlarmBottomSheetContent(
            currentDuration = 5,
            onDurationSelect = {}
        )
    }
}
