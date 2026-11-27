package vn.io.litever.remind.features.alarms.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.designsystem.components.ReMindBottomSheetContent
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsDivider
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.features.alarms.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GentleAlarmBottomSheet(
    currentDuration: Int,
    onDurationSelect: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    options: List<Int> = listOf(0, 5, 10, 20),
    sheetState: SheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        ReMindBottomSheetContent(
            title = stringResource(R.string.gentle_alarm_title)
        ) {
            GentleAlarmBottomSheetContent(
                currentDuration = currentDuration,
                onDurationSelect = onDurationSelect,
                options = options
            )
        }
    }
}

@Composable
fun GentleAlarmBottomSheetContent(
    currentDuration: Int,
    onDurationSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    options: List<Int> = listOf(0, 5, 10, 20)
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = MaterialTheme.spacing.medium)
            .navigationBarsPadding()
    ) {
        Text(
            text = stringResource(R.string.gentle_alarm_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.small
            )
        )

        options.forEachIndexed { index, option ->
            val isSelected = currentDuration == option
            val label = when {
                option == 0 -> stringResource(R.string.off)
                option < 60 -> stringResource(R.string.seconds_unit, option)
                else -> stringResource(R.string.minutes_unit, option / 60)
            }

            ListItem(
                leadingContent = {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onDurationSelect(option) }
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { onDurationSelect(option) }
            ) {
                Text(
                    text = label,
                    style = if (isSelected) {
                        LiteverTokens.typography.bodyLargeEmphasized
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            if (index < options.lastIndex) {
                ReMindSettingsDivider()
            }
        }
    }
}

@Preview(name = "Gentle Alarm - Light", showBackground = true)
@Composable
private fun GentleAlarmBottomSheetContentPreview() {
    ReMindTheme {
        GentleAlarmBottomSheetContent(
            currentDuration = 10,
            onDurationSelect = {}
        )
    }
}

@Preview(
    name = "Gentle Alarm - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF19120CL
)
@Composable
private fun GentleAlarmBottomSheetContentDarkPreview() {
    ReMindTheme(darkTheme = true) {
        GentleAlarmBottomSheetContent(
            currentDuration = 10,
            onDurationSelect = {}
        )
    }
}
