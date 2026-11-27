package vn.io.litever.remind.features.alarms.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.designsystem.components.ReMindBottomSheetContent
import vn.io.litever.remind.core.designsystem.components.ReMindProTag
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsDivider
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
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
    sheetState: SheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
) {
    var draftMinutes by remember(currentMinutes) { mutableIntStateOf(currentMinutes) }
    val requiresPro = !isPro && draftMinutes > 0

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        ReMindBottomSheetContent(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Text(text = stringResource(R.string.wakeup_check_title))
                    if (!isPro) {
                        ReMindProTag()
                    }
                }
            }
        ) {
            WakeUpCheckBottomSheetContent(
                selectedMinutes = draftMinutes,
                requiresPro = requiresPro,
                onMinutesChange = { draftMinutes = it },
                onSaveClick = {
                    if (requiresPro) {
                        onDismissRequest()
                        onNavigateToPaywall(3)
                    } else {
                        onSelectMinutes(draftMinutes)
                        onDismissRequest()
                    }
                }
            )
        }
    }
}

@Composable
fun WakeUpCheckBottomSheetContent(
    selectedMinutes: Int,
    requiresPro: Boolean,
    onMinutesChange: (Int) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(0, 1, 3, 5, 10)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = MaterialTheme.spacing.medium)
            .navigationBarsPadding()
    ) {
        Text(
            text = stringResource(R.string.wakeup_check_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.small
            )
        )

        options.forEachIndexed { index, option ->
            val isSelected = selectedMinutes == option
            val label = if (option == 0) {
                stringResource(R.string.off)
            } else {
                stringResource(R.string.minutes_unit, option)
            }
            ListItem(
                leadingContent = {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onMinutesChange(option) }
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { onMinutesChange(option) }
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

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .height(48.dp)
        ) {
            if (requiresPro) {
                Icon(
                    imageVector = ReMindIcons.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            }
            Text(text = stringResource(R.string.save))
        }
    }
}

@Preview(name = "Wake-Up Check - Light", showBackground = true)
@Composable
private fun WakeUpCheckBottomSheetLightPreview() {
    ReMindTheme {
        WakeUpCheckBottomSheetContent(
            selectedMinutes = 5,
            requiresPro = true,
            onMinutesChange = {},
            onSaveClick = {}
        )
    }
}

@Preview(
    name = "Wake-Up Check - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF19120CL
)
@Composable
private fun WakeUpCheckBottomSheetDarkPreview() {
    ReMindTheme(darkTheme = true) {
        WakeUpCheckBottomSheetContent(
            selectedMinutes = 3,
            requiresPro = false,
            onMinutesChange = {},
            onSaveClick = {}
        )
    }
}
