package vn.io.litever.remind.features.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.designsystem.components.LiteverListItem
import vn.io.litever.designsystem.components.LiteverRadioButton
import vn.io.litever.designsystem.components.LiteverScaffold
import vn.io.litever.designsystem.components.LiteverSegmentedButton
import vn.io.litever.designsystem.components.LiteverSettingsGroup
import vn.io.litever.designsystem.components.LiteverSingleChoiceSegmentedButtonRow
import vn.io.litever.designsystem.components.LiteverTopAppBar
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.BrandLogo
import vn.io.litever.remind.core.designsystem.components.ReMindLogo
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
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
    LiteverScaffold(
        topBar = {
            LiteverTopAppBar(
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
                LiteverSettingsGroup(title = stringResource(R.string.hour_format_24_headline)) {
                    val timeOptions = listOf(
                        "SYSTEM" to stringResource(R.string.time_format_system),
                        "H12" to stringResource(R.string.time_format_12h),
                        "H24" to stringResource(R.string.time_format_24h)
                    )

                    LiteverSingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        timeOptions.forEachIndexed { _, pair ->
                            LiteverSegmentedButton(
                                selected = uiState.timeFormat == pair.first,
                                onClick = { onTimeFormatChange(pair.first) },
                                label = {
                                    Text(pair.second)
                                }
                            )
                        }
                    }
                }
            }

            // Display Group
            item {
                LiteverSettingsGroup(title = stringResource(R.string.display_headline)) {
                    Text(
                        text = stringResource(R.string.display_mode_headline),
                        style = LiteverTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                    )

                    val options = listOf(
                        "SYSTEM" to stringResource(R.string.theme_system),
                        "LIGHT" to stringResource(R.string.theme_light),
                        "DARK" to stringResource(R.string.theme_dark)
                    )
                    val icons = listOf(Icons.Rounded.SettingsBrightness, Icons.Rounded.LightMode, Icons.Rounded.DarkMode)

                    LiteverSingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        options.forEachIndexed { index, pair ->
                            LiteverSegmentedButton(
                                selected = uiState.themeMode == pair.first,
                                onClick = { onThemeModeChange(pair.first) },
                                icon = {
                                    SegmentedButtonDefaults.Icon(active = uiState.themeMode == pair.first) {
                                        Icon(
                                            imageVector = icons[index],
                                            contentDescription = pair.second,
                                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                        )
                                    }
                                },
                                label = { Text(pair.second) }
                            )
                        }
                    }

                    Spacer(Modifier.height(LiteverTheme.spacing.large))

                    Text(
                        text = stringResource(R.string.color_source_headline),
                        style = LiteverTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    )

                    val colorOptions = listOf(
                        "REMIND" to stringResource(R.string.color_source_remind),
                        "LITEVER" to stringResource(R.string.color_source_litever),
                        "DYNAMIC" to stringResource(R.string.color_source_wallpaper),
                    )

                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        colorOptions.forEach { pair ->
                            LiteverListItem(
                                headlineContent = {
                                    when (pair.first) {
                                        "REMIND" -> {
                                            ReMindLogo(
                                                fontSize = LiteverTheme.typography.bodyLarge.fontSize
                                            )
                                        }
                                        "LITEVER" -> {
                                            BrandLogo(
                                                fontSize = LiteverTheme.typography.bodyLarge.fontSize
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = pair.second,
                                                style = LiteverTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }
                                    }
                                },
                                leadingContent = {
                                    LiteverRadioButton(
                                        selected = uiState.colorPalette == pair.first,
                                        onClick = { onColorPaletteChange(pair.first) }
                                    )
                                },
                                modifier = Modifier.clickable { onColorPaletteChange(pair.first) }
                            )
                        }
                    }
                }
            }

            // Language Group
            item {
                LiteverSettingsGroup(title = stringResource(R.string.language_headline)) {
                    val languageOptions = listOf(
                        "en" to stringResource(R.string.language_english),
                        "vi" to stringResource(R.string.language_vietnamese)
                    )

                    LiteverSingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        languageOptions.forEachIndexed { _, pair ->
                            LiteverSegmentedButton(
                                selected = uiState.language == pair.first,
                                onClick = { onLanguageChange(pair.first) },
                                label = { Text(pair.second) }
                            )
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
    ReMindTheme {
        GeneralSettingsScreen(
            uiState = SettingsUiState(is24HourFormat = true, timeFormat = "SYSTEM", themeMode = "SYSTEM", colorPalette = "REMIND"),
            onTimeFormatChange = {},
            onThemeModeChange = {},
            onColorPaletteChange = {},
            onLanguageChange = {},
            onNavigateBack = {}
        )
    }
}










