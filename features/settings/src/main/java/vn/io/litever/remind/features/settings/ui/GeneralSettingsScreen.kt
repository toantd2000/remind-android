package vn.io.litever.remind.features.settings.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.button.LvIconButton
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.designsystem.theme.palettes.blueDarkColorScheme
import vn.io.litever.designsystem.theme.palettes.blueLightColorScheme
import vn.io.litever.designsystem.theme.palettes.greenDarkColorScheme
import vn.io.litever.designsystem.theme.palettes.greenLightColorScheme
import vn.io.litever.designsystem.theme.palettes.indigoDarkColorScheme
import vn.io.litever.designsystem.theme.palettes.indigoLightColorScheme
import vn.io.litever.designsystem.theme.palettes.orangeDarkColorScheme
import vn.io.litever.designsystem.theme.palettes.orangeLightColorScheme
import vn.io.litever.designsystem.theme.palettes.redDarkColorScheme
import vn.io.litever.designsystem.theme.palettes.redLightColorScheme
import vn.io.litever.designsystem.theme.palettes.violetDarkColorScheme
import vn.io.litever.designsystem.theme.palettes.violetLightColorScheme
import vn.io.litever.designsystem.theme.palettes.yellowDarkColorScheme
import vn.io.litever.designsystem.theme.palettes.yellowLightColorScheme
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.components.ReMindTopAppBar
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
    Scaffold(
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
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
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
                            .padding(LiteverTheme.spacing.medium)
                    ) {
                        timeOptions.forEachIndexed { index, pair ->
                            SegmentedButton(
                                selected = uiState.timeFormat == pair.first,
                                onClick = { onTimeFormatChange(pair.first) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = timeOptions.size),
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
                ReMindSettingsGroup(title = stringResource(R.string.display_headline)) {
                    Text(
                        text = stringResource(R.string.display_mode_headline),
                        style = LiteverTheme.typography.titleSmall,
                        modifier = Modifier.padding(
                            start = LiteverTheme.spacing.medium,
                            end = LiteverTheme.spacing.medium,
                            top = LiteverTheme.spacing.small,
                            bottom = LiteverTheme.spacing.small
                        )
                    )

                    val options = listOf(
                        "SYSTEM" to stringResource(R.string.theme_system),
                        "LIGHT" to stringResource(R.string.theme_light),
                        "DARK" to stringResource(R.string.theme_dark)
                    )
                    val icons = listOf(Icons.Rounded.SettingsBrightness, Icons.Rounded.LightMode, Icons.Rounded.DarkMode)

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = LiteverTheme.spacing.medium)
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
                                },
                                label = { Text(pair.second) }
                            )
                        }
                    }

                    Spacer(Modifier.height(LiteverTheme.spacing.smallMedium))

                    Text(
                        text = stringResource(R.string.color_source_headline),
                        style = LiteverTheme.typography.titleSmall,
                        modifier = Modifier.padding(
                            start = LiteverTheme.spacing.medium,
                            end = LiteverTheme.spacing.medium,
                            bottom = LiteverTheme.spacing.small
                        )
                    )

                    val isDark = when (uiState.themeMode) {
                        "LIGHT" -> false
                        "DARK" -> true
                        else -> isSystemInDarkTheme()
                    }

                    val context = LocalContext.current
                    val dynamicLight = remember(context) {
                        dynamicLightColorScheme(context)
                    }
                    val dynamicDark = remember(context) {
                        dynamicDarkColorScheme(context)
                    }

                    val colorPaletteOptions = remember(dynamicLight, dynamicDark) {
                        listOf(
                            PaletteColorOption(
                                key = "RED",
                                titleRes = R.string.color_red,
                                primaryLight = redLightColorScheme.primary,
                                primaryContainerLight = redLightColorScheme.primaryContainer,
                                primaryDark = redDarkColorScheme.primary,
                                primaryContainerDark = redDarkColorScheme.primaryContainer
                            ),
                            PaletteColorOption(
                                key = "ORANGE",
                                titleRes = R.string.color_orange,
                                primaryLight = orangeLightColorScheme.primary,
                                primaryContainerLight = orangeLightColorScheme.primaryContainer,
                                primaryDark = orangeDarkColorScheme.primary,
                                primaryContainerDark = orangeDarkColorScheme.primaryContainer
                            ),
                            PaletteColorOption(
                                key = "YELLOW",
                                titleRes = R.string.color_yellow,
                                primaryLight = yellowLightColorScheme.primary,
                                primaryContainerLight = yellowLightColorScheme.primaryContainer,
                                primaryDark = yellowDarkColorScheme.primary,
                                primaryContainerDark = yellowDarkColorScheme.primaryContainer
                            ),
                            PaletteColorOption(
                                key = "GREEN",
                                titleRes = R.string.color_green,
                                primaryLight = greenLightColorScheme.primary,
                                primaryContainerLight = greenLightColorScheme.primaryContainer,
                                primaryDark = greenDarkColorScheme.primary,
                                primaryContainerDark = greenDarkColorScheme.primaryContainer
                            ),
                            PaletteColorOption(
                                key = "BLUE",
                                titleRes = R.string.color_blue,
                                primaryLight = blueLightColorScheme.primary,
                                primaryContainerLight = blueLightColorScheme.primaryContainer,
                                primaryDark = blueDarkColorScheme.primary,
                                primaryContainerDark = blueDarkColorScheme.primaryContainer
                            ),
                            PaletteColorOption(
                                key = "INDIGO",
                                titleRes = R.string.color_indigo,
                                primaryLight = indigoLightColorScheme.primary,
                                primaryContainerLight = indigoLightColorScheme.primaryContainer,
                                primaryDark = indigoDarkColorScheme.primary,
                                primaryContainerDark = indigoDarkColorScheme.primaryContainer
                            ),
                            PaletteColorOption(
                                key = "VIOLET",
                                titleRes = R.string.color_violet,
                                primaryLight = violetLightColorScheme.primary,
                                primaryContainerLight = violetLightColorScheme.primaryContainer,
                                primaryDark = violetDarkColorScheme.primary,
                                primaryContainerDark = violetDarkColorScheme.primaryContainer
                            ),
                            PaletteColorOption(
                                key = "DYNAMIC",
                                titleRes = R.string.color_screen,
                                primaryLight = dynamicLight.primary,
                                primaryContainerLight = dynamicLight.primaryContainer,
                                primaryDark = dynamicDark.primary,
                                primaryContainerDark = dynamicDark.primaryContainer
                            )
                        )
                    }

                    val row1 = colorPaletteOptions.take(4)
                    val row2 = colorPaletteOptions.drop(4)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = LiteverTheme.spacing.medium,
                                end = LiteverTheme.spacing.medium,
                                bottom = LiteverTheme.spacing.medium
                            ),
                        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.extraSmall)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.extraSmall)
                        ) {
                            row1.forEach { item ->
                                ColorPaletteCell(
                                    item = item,
                                    isSelected = uiState.colorPalette == item.key,
                                    isDark = isDark,
                                    onClick = { onColorPaletteChange(item.key) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.extraSmall)
                        ) {
                            row2.forEach { item ->
                                ColorPaletteCell(
                                    item = item,
                                    isSelected = uiState.colorPalette == item.key,
                                    isDark = isDark,
                                    onClick = { onColorPaletteChange(item.key) },
                                    modifier = Modifier.weight(1f)
                                )
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
                            .padding(LiteverTheme.spacing.medium)
                    ) {
                        languageOptions.forEachIndexed { index, pair ->
                            SegmentedButton(
                                selected = uiState.language == pair.first,
                                onClick = { onLanguageChange(pair.first) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = languageOptions.size),
                                label = { Text(pair.second) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class PaletteColorOption(
    val key: String,
    val titleRes: Int,
    val primaryLight: Color,
    val primaryContainerLight: Color,
    val primaryDark: Color,
    val primaryContainerDark: Color
)

@Composable
private fun ColorPaletteCell(
    item: PaletteColorOption,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = if (isDark) item.primaryDark else item.primaryLight
    val containerColor = if (isDark) item.primaryContainerDark else item.primaryContainerLight

    LvIconButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        type = if (isSelected) LvButtonType.Outlined else LvButtonType.Tonal,
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = containerColor,
            contentColor = primaryColor,
        ),
        shape = LiteverTheme.shapes.extraSmall,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = LiteverTheme.spacing.tiny),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 2.dp)
                )
            }
            Text(
                text = stringResource(item.titleRes),
                color = primaryColor,
                style = LiteverTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun GeneralSettingsScreenPreview() {
    ReMindTheme {
        GeneralSettingsScreen(
            uiState = SettingsUiState(is24HourFormat = true, timeFormat = "SYSTEM", themeMode = "SYSTEM", colorPalette = "RED"),
            onTimeFormatChange = {},
            onThemeModeChange = {},
            onColorPaletteChange = {},
            onLanguageChange = {},
            onNavigateBack = {}
        )
    }
}










