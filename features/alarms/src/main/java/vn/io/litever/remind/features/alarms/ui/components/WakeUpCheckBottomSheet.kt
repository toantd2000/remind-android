package vn.io.litever.remind.features.alarms.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindBottomSheetContent
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.alarms.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WakeUpCheckBottomSheet(
    currentMinutes: Int,
    isPro: Boolean,
    onSelectMinutes: (Int) -> Unit,
    onNavigateToPaywall: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var draftMinutes by remember(currentMinutes) { mutableIntStateOf(currentMinutes) }
    val requiresPro = !isPro && draftMinutes > 0
    val options = listOf(0, 1, 3, 5, 10)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = LiteverTheme.colors.surface,
        modifier = modifier
    ) {
        ReMindBottomSheetContent(
            title = stringResource(R.string.wakeup_check_title)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = LiteverTheme.spacing.medium)
            ) {
                Text(
                    text = stringResource(R.string.wakeup_check_desc),
                    style = LiteverTheme.typography.bodyMedium,
                    color = LiteverTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(
                        horizontal = LiteverTheme.spacing.medium,
                        vertical = LiteverTheme.spacing.small
                    )
                )

                options.forEach { option ->
                    val label = if (option == 0) {
                        stringResource(R.string.off)
                    } else {
                        stringResource(R.string.minutes_unit, option)
                    }
                    ListItem(
                        headlineContent = {
                            Text(
                                text = label,
                                style = LiteverTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                            )
                        },
                        leadingContent = {
                            RadioButton(
                                selected = draftMinutes == option,
                                onClick = { draftMinutes = option }
                            )
                        },
                        modifier = Modifier.clickable { draftMinutes = option }
                    )
                }

                Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))

                Button(
                    onClick = {
                        if (requiresPro) {
                            onDismissRequest()
                            onNavigateToPaywall(3)
                        } else {
                            onSelectMinutes(draftMinutes)
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LiteverTheme.spacing.medium)
                        .height(48.dp)
                ) {
                    if (requiresPro) {
                        Icon(
                            imageVector = ReMindIcons.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = stringResource(R.string.save),
                        style = LiteverTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WakeUpCheckBottomSheetPreview() {
    ReMindTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = "Wake-up Check Preview", style = LiteverTheme.typography.titleMedium)
        }
    }
}
