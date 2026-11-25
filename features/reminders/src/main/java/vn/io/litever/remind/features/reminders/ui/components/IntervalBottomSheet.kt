package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.core.designsystem.components.ReMindBottomSheetContent
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.features.reminders.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervalBottomSheet(
    currentIntervalMinutes: Int,
    onDismissRequest: () -> Unit,
    onConfirm: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        ReMindBottomSheetContent(
            title = stringResource(R.string.interval_section_title)
        ) {
            IntervalBottomSheetContent(
                currentIntervalMinutes = currentIntervalMinutes,
                onDismiss = onDismissRequest,
                onConfirm = onConfirm
            )
        }
    }
}

@Composable
fun IntervalBottomSheetContent(
    currentIntervalMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var totalMinutes by remember { mutableIntStateOf(currentIntervalMinutes) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
            .padding(bottom = MaterialTheme.spacing.medium)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Increment Display (+ 0h / + 0m) and Reset button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            val displayText = when {
                hours > 0 && minutes > 0 -> "+ ${hours}h ${minutes}m"
                hours > 0 -> "+ ${hours}h"
                else -> "+ ${totalMinutes}m"
            }

            Text(
                text = displayText,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

            IconButton(
                onClick = { totalMinutes = 0 },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = ReMindIcons.Refresh,
                    contentDescription = stringResource(R.string.reset),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // 2. Preset Time Grid (2 rows x 3 buttons)
        val presetsRow1 = listOf(15 to R.string.interval_preset_15m, 30 to R.string.interval_preset_30m, 60 to R.string.interval_preset_1h)
        val presetsRow2 = listOf(120 to R.string.interval_preset_2h, 180 to R.string.interval_preset_3h, 240 to R.string.interval_preset_4h)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            presetsRow1.forEach { (mins, resId) ->
                FilledTonalButton(
                    onClick = { totalMinutes += mins },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(resId))
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            presetsRow2.forEach { (mins, resId) ->
                FilledTonalButton(
                    onClick = { totalMinutes += mins },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(resId))
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.mediumLarge))

        // 3. Action Buttons (Cancel & Save)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }

            Button(
                onClick = {
                    if (totalMinutes > 0) {
                        onConfirm(totalMinutes)
                    }
                },
                enabled = totalMinutes > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.action_save))
            }
        }
    }
}

@Preview(name = "Interval Sheet - Light", showBackground = true)
@Composable
private fun IntervalBottomSheetContentPreview() {
    ReMindTheme {
        IntervalBottomSheetContent(
            currentIntervalMinutes = 30,
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(
    name = "Interval Sheet - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF19120CL
)
@Composable
private fun IntervalBottomSheetContentDarkPreview() {
    ReMindTheme(darkTheme = true) {
        IntervalBottomSheetContent(
            currentIntervalMinutes = 30,
            onDismiss = {},
            onConfirm = {}
        )
    }
}
