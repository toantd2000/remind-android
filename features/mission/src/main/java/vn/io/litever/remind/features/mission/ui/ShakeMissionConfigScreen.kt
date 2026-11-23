package vn.io.litever.remind.features.mission.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.ShakeMissionConfig
import vn.io.litever.remind.core.model.ShakeSensitivity
import vn.io.litever.remind.features.mission.R

@Composable
fun ShakeMissionConfigRoute(
    alarmId: Long,
    initialTargetCount: Int,
    initialSensitivity: ShakeSensitivity,
    onBackClick: () -> Unit,
    onSaveMission: (Mission) -> Unit
) {
    ShakeMissionConfigScreen(
        initialTargetCount = initialTargetCount,
        initialSensitivity = initialSensitivity,
        onBackClick = onBackClick,
        onSaveClick = { count, sensitivity ->
            onSaveMission(
                Mission(
                    alarmId = alarmId,
                    type = MissionType.SHAKE,
                    order = 0,
                    repeatCount = 1,
                    config = ShakeMissionConfig(targetCount = count, sensitivity = sensitivity)
                )
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShakeMissionConfigScreen(
    initialTargetCount: Int = 30,
    initialSensitivity: ShakeSensitivity = ShakeSensitivity.NORMAL,
    onBackClick: () -> Unit,
    onSaveClick: (Int, ShakeSensitivity) -> Unit
) {
    var targetCount by remember { mutableIntStateOf(initialTargetCount.coerceIn(10, 100)) }
    var sensitivity by remember { mutableStateOf(initialSensitivity) }

    val countOptions = listOf(15, 30, 50, 80)

    Scaffold(
        topBar = {
            LvTopAppBar(
                title = stringResource(R.string.shake_config_title),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ReMindBottomBar {
                Button(
                    onClick = { onSaveClick(targetCount, sensitivity) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_mission_action))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(LiteverTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
        ) {
            ReMindSettingsGroup(title = stringResource(R.string.shake_count_label)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(LiteverTheme.spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    countOptions.forEach { count ->
                        FilterChip(
                            selected = targetCount == count,
                            onClick = { targetCount = count },
                            label = {
                                Text(
                                    text = "$count",
                                    style = LiteverTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            ReMindSettingsGroup(title = stringResource(R.string.shake_sensitivity_label)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(LiteverTheme.spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShakeSensitivity.entries.forEach { level ->
                        val labelRes = when (level) {
                            ShakeSensitivity.EASY -> R.string.shake_sensitivity_easy
                            ShakeSensitivity.NORMAL -> R.string.shake_sensitivity_normal
                            ShakeSensitivity.HARD -> R.string.shake_sensitivity_hard
                        }
                        FilterChip(
                            selected = sensitivity == level,
                            onClick = { sensitivity = level },
                            label = { Text(stringResource(labelRes)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShakeMissionConfigScreenPreview() {
    ReMindTheme {
        ShakeMissionConfigScreen(
            onBackClick = {},
            onSaveClick = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShakeMissionConfigScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ShakeMissionConfigScreen(
            onBackClick = {},
            onSaveClick = { _, _ -> }
        )
    }
}
