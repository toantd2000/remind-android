package vn.io.litever.alarm.features.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import vn.io.litever.alarm.features.settings.R
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        uiState = uiState,
        onTimeFormatChange = viewModel::setTimeFormat,
        onThemeModeChange = viewModel::setThemeMode,
        onColorPaletteChange = viewModel::setColorPalette
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onTimeFormatChange: (String) -> Unit,
    onThemeModeChange: (String) -> Unit,
    onColorPaletteChange: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Column {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = stringResource(R.string.hour_format_24_headline),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            
                            val timeOptions = listOf(
                                "SYSTEM" to stringResource(R.string.time_format_system),
                                "H12" to stringResource(R.string.time_format_12h),
                                "H24" to stringResource(R.string.time_format_24h)
                            )
                            
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = 0.dp,
                                        bottom = 8.dp,
                                        start = 16.dp,
                                        end = 16.dp
                                    )
                            ) {
                                timeOptions.forEachIndexed { index, pair ->
                                    SegmentedButton(
                                        selected = uiState.timeFormat == pair.first,
                                        onClick = { onTimeFormatChange(pair.first) },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = timeOptions.size),
                                    ) {
                                        Text(pair.second)
                                    }
                                }
                            }
                        }
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = stringResource(R.string.appearance_and_color_group),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.display_mode_headline)) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                supportingContent = {
                                    val options = listOf(
                                        "SYSTEM" to stringResource(R.string.theme_system),
                                        "LIGHT" to stringResource(R.string.theme_light),
                                        "DARK" to stringResource(R.string.theme_dark)
                                    )
                                    val icons = listOf(Icons.Default.SettingsBrightness, Icons.Default.LightMode, Icons.Default.DarkMode)
                                    
                                    SingleChoiceSegmentedButtonRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    ) {
                                        options.forEachIndexed { index, pair ->
                                            SegmentedButton(
                                                selected = uiState.themeMode == pair.first,
                                                onClick = { onThemeModeChange(pair.first) },
                                                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                                                icon = {
                                                    SegmentedButtonDefaults.Icon(active = uiState.themeMode == pair.first) {
                                                        Icon(
                                                            imageVector = icons[index],
                                                            contentDescription = pair.second,
                                                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                                        )
                                                    }
                                                }
                                            ) {
                                                Text(pair.second)
                                            }
                                        }
                                    }
                                }
                            )

                            ListItem(
                                headlineContent = { Text(stringResource(R.string.color_source_headline)) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                supportingContent = {
                                    val colorOptions = listOf(
                                        "DEFAULT" to stringResource(R.string.color_source_default),
                                        "DYNAMIC" to stringResource(R.string.color_source_wallpaper)
                                    )
                                    
                                    SingleChoiceSegmentedButtonRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    ) {
                                        colorOptions.forEachIndexed { index, pair ->
                                            SegmentedButton(
                                                selected = uiState.colorPalette == pair.first,
                                                onClick = { onColorPaletteChange(pair.first) },
                                                shape = SegmentedButtonDefaults.itemShape(index = index, count = colorOptions.size),
                                                icon = {
                                                    SegmentedButtonDefaults.Icon(active = uiState.colorPalette == pair.first) {
                                                        Icon(
                                                            imageVector = if (pair.first == "DYNAMIC") Icons.Default.Wallpaper else Icons.Default.Palette,
                                                            contentDescription = pair.second,
                                                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                                        )
                                                    }
                                                }
                                            ) {
                                                Text(pair.second)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // TODO: Get real version dynamically
                Text(text = stringResource(R.string.app_version_format, "1.0", 1))
            }
        }
    }
}

// ColorDot removed as requested

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    vn.io.litever.alarm.core.designsystem.theme.AlarmTheme {
        SettingsScreen(
            uiState = SettingsUiState(is24HourFormat = true, timeFormat = "SYSTEM", themeMode = "SYSTEM", colorPalette = "DEFAULT"),
            onTimeFormatChange = {},
            onThemeModeChange = {},
            onColorPaletteChange = {}
        )
    }
}
