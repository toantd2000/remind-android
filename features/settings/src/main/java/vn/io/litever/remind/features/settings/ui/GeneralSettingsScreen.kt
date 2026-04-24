package vn.io.litever.remind.features.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import vn.io.litever.remind.core.designsystem.components.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.remind.features.settings.R

@Composable
fun GeneralSettingsRoute(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    GeneralSettingsScreen(
        uiState = uiState,
        onTimeFormatChange = viewModel::setTimeFormat,
        onThemeModeChange = viewModel::setThemeMode,
        onColorPaletteChange = viewModel::setColorPalette,
        onLanguageChange = viewModel::setLanguage,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(
    uiState: SettingsUiState,
    onTimeFormatChange: (String) -> Unit,
    onThemeModeChange: (String) -> Unit,
    onColorPaletteChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    ReMindScaffold(
        topBar = {
            ReMindTopAppBar(
                title = stringResource(R.string.setting_general_title),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Hour Format Group
            item {
                ReMindSettingsGroup(title = stringResource(R.string.hour_format_24_headline)) {
                    val timeOptions = listOf(
                        "SYSTEM" to stringResource(R.string.time_format_system),
                        "H12" to stringResource(R.string.time_format_12h),
                        "H24" to stringResource(R.string.time_format_24h)
                    )

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
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

            // Display Mode Group
            item {
                ReMindSettingsGroup(title = stringResource(R.string.display_mode_headline)) {
                    val options = listOf(
                        "SYSTEM" to stringResource(R.string.theme_system),
                        "LIGHT" to stringResource(R.string.theme_light),
                        "DARK" to stringResource(R.string.theme_dark)
                    )
                    val icons = listOf(Icons.Rounded.SettingsBrightness, Icons.Rounded.LightMode, Icons.Rounded.DarkMode)

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
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
            }

            // Color Source Group
            item {
                ReMindSettingsGroup(title = stringResource(R.string.color_source_headline)) {
                    val colorOptions = listOf(
                        "DEFAULT" to stringResource(R.string.color_source_default),
                        "DYNAMIC" to stringResource(R.string.color_source_wallpaper)
                    )

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        colorOptions.forEachIndexed { index, pair ->
                            SegmentedButton(
                                selected = uiState.colorPalette == pair.first,
                                onClick = { onColorPaletteChange(pair.first) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = colorOptions.size),
                                icon = {
                                    SegmentedButtonDefaults.Icon(active = uiState.colorPalette == pair.first) {
                                        Icon(
                                            imageVector = if (pair.first == "DYNAMIC") Icons.Rounded.Wallpaper else Icons.Rounded.Palette,
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
            }

            // Language Group
            item {
                ReMindSettingsGroup(title = stringResource(R.string.language_headline)) {
                    val languageOptions = listOf(
                        "en" to stringResource(R.string.language_english),
                        "vi" to stringResource(R.string.language_vietnamese)
                    )

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        languageOptions.forEachIndexed { index, pair ->
                            SegmentedButton(
                                selected = uiState.language == pair.first,
                                onClick = { onLanguageChange(pair.first) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = languageOptions.size),
                            ) {
                                Text(pair.second)
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun GeneralSettingsScreenPreview() {
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        GeneralSettingsScreen(
            uiState = SettingsUiState(is24HourFormat = true, timeFormat = "SYSTEM", themeMode = "SYSTEM", colorPalette = "DEFAULT"),
            onTimeFormatChange = {},
            onThemeModeChange = {},
            onColorPaletteChange = {},
            onLanguageChange = {},
            onNavigateBack = {}
        )
    }
}










