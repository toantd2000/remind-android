package vn.io.litever.remind.features.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.brand.tokens.palettes.blackDarkColorScheme
import vn.io.litever.brand.tokens.palettes.blackLightColorScheme
import vn.io.litever.brand.tokens.palettes.blueDarkColorScheme
import vn.io.litever.brand.tokens.palettes.blueLightColorScheme
import vn.io.litever.brand.tokens.palettes.greenDarkColorScheme
import vn.io.litever.brand.tokens.palettes.greenLightColorScheme
import vn.io.litever.brand.tokens.palettes.indigoDarkColorScheme
import vn.io.litever.brand.tokens.palettes.indigoLightColorScheme
import vn.io.litever.brand.tokens.palettes.orangeDarkColorScheme
import vn.io.litever.brand.tokens.palettes.orangeLightColorScheme
import vn.io.litever.brand.tokens.palettes.redDarkColorScheme
import vn.io.litever.brand.tokens.palettes.redLightColorScheme
import vn.io.litever.brand.tokens.palettes.violetDarkColorScheme
import vn.io.litever.brand.tokens.palettes.violetLightColorScheme
import vn.io.litever.brand.tokens.palettes.yellowDarkColorScheme
import vn.io.litever.brand.tokens.palettes.yellowLightColorScheme
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsItem
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
    var showTimeFormatDialog by remember { mutableStateOf(false) }
    var showThemeModeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showColorPaletteDialog by remember { mutableStateOf(false) }

    val isDark = when (uiState.themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val dynamicLight = remember(context) { dynamicLightColorScheme(context) }
    val dynamicDark = remember(context) { dynamicDarkColorScheme(context) }

    val colorPaletteOptions = remember(dynamicLight, dynamicDark) {
        listOf(
            PaletteColorOption(
                key = "BLACK",
                titleRes = R.string.color_black,
                primaryLight = blackLightColorScheme.primary,
                onPrimaryLight = blackLightColorScheme.onPrimary,
                primaryDark = blackDarkColorScheme.primary,
                onPrimaryDark = blackDarkColorScheme.onPrimary
            ),
            PaletteColorOption(
                key = "RED",
                titleRes = R.string.color_red,
                primaryLight = redLightColorScheme.primary,
                onPrimaryLight = redLightColorScheme.onPrimary,
                primaryDark = redDarkColorScheme.primary,
                onPrimaryDark = redDarkColorScheme.onPrimary
            ),
            PaletteColorOption(
                key = "ORANGE",
                titleRes = R.string.color_orange,
                primaryLight = orangeLightColorScheme.primary,
                onPrimaryLight = orangeLightColorScheme.onPrimary,
                primaryDark = orangeDarkColorScheme.primary,
                onPrimaryDark = orangeDarkColorScheme.onPrimary
            ),
            PaletteColorOption(
                key = "YELLOW",
                titleRes = R.string.color_yellow,
                primaryLight = yellowLightColorScheme.primary,
                onPrimaryLight = yellowLightColorScheme.onPrimary,
                primaryDark = yellowDarkColorScheme.primary,
                onPrimaryDark = yellowDarkColorScheme.onPrimary
            ),
            PaletteColorOption(
                key = "GREEN",
                titleRes = R.string.color_green,
                primaryLight = greenLightColorScheme.primary,
                onPrimaryLight = greenLightColorScheme.onPrimary,
                primaryDark = greenDarkColorScheme.primary,
                onPrimaryDark = greenDarkColorScheme.onPrimary
            ),
            PaletteColorOption(
                key = "BLUE",
                titleRes = R.string.color_blue,
                primaryLight = blueLightColorScheme.primary,
                onPrimaryLight = blueLightColorScheme.onPrimary,
                primaryDark = blueDarkColorScheme.primary,
                onPrimaryDark = blueDarkColorScheme.onPrimary
            ),
            PaletteColorOption(
                key = "INDIGO",
                titleRes = R.string.color_indigo,
                primaryLight = indigoLightColorScheme.primary,
                onPrimaryLight = indigoLightColorScheme.onPrimary,
                primaryDark = indigoDarkColorScheme.primary,
                onPrimaryDark = indigoDarkColorScheme.onPrimary
            ),
            PaletteColorOption(
                key = "VIOLET",
                titleRes = R.string.color_violet,
                primaryLight = violetLightColorScheme.primary,
                onPrimaryLight = violetLightColorScheme.onPrimary,
                primaryDark = violetDarkColorScheme.primary,
                onPrimaryDark = violetDarkColorScheme.onPrimary
            ),
            PaletteColorOption(
                key = "DYNAMIC",
                titleRes = R.string.color_screen,
                primaryLight = dynamicLight.primary,
                onPrimaryLight = dynamicLight.onPrimary,
                primaryDark = dynamicDark.primary,
                onPrimaryDark = dynamicDark.onPrimary
            )
        )
    }

    val selectedColorOption = colorPaletteOptions.firstOrNull { it.key == uiState.colorPalette }
        ?: colorPaletteOptions.first()
    val currentColorPaletteLabel = stringResource(selectedColorOption.titleRes)
    val currentColorSwatch = if (isDark) selectedColorOption.primaryDark else selectedColorOption.primaryLight

    val timeOptions = listOf(
        "SYSTEM" to stringResource(R.string.time_format_system),
        "H12" to stringResource(R.string.time_format_12h),
        "H24" to stringResource(R.string.time_format_24h)
    )

    val themeOptions = listOf(
        "SYSTEM" to stringResource(R.string.theme_system),
        "LIGHT" to stringResource(R.string.theme_light),
        "DARK" to stringResource(R.string.theme_dark)
    )

    val languageOptions = listOf(
        "en" to stringResource(R.string.language_english),
        "vi" to stringResource(R.string.language_vietnamese)
    )

    val currentTimeFormatLabel = timeOptions.firstOrNull { it.first == uiState.timeFormat }?.second
        ?: stringResource(R.string.time_format_system)
    val currentThemeLabel = themeOptions.firstOrNull { it.first == uiState.themeMode }?.second
        ?: stringResource(R.string.theme_system)
    val currentLanguageLabel = languageOptions.firstOrNull { it.first == uiState.language }?.second
        ?: stringResource(R.string.language_english)

    val themeIcon = when (uiState.themeMode) {
        "LIGHT" -> ReMindIcons.LightMode
        "DARK" -> ReMindIcons.DarkMode
        else -> ReMindIcons.BrightnessMedium
    }

    Scaffold(
        topBar = {
            LvTopAppBar(
                title = stringResource(R.string.setting_general_title),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(LiteverTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
        ) {
            // Display Group
            item {
                ReMindSettingsGroup(title = stringResource(R.string.display_headline)) {
                    ReMindSettingsItem(
                        title = stringResource(R.string.display_mode_headline),
                        subtitle = currentThemeLabel,
                        icon = themeIcon,
                        trailingContent = {
                            Icon(
                                imageVector = ReMindIcons.ChevronRight,
                                contentDescription = null,
                                tint = LiteverTheme.colors.onSurfaceVariant
                            )
                        },
                        onClick = { showThemeModeDialog = true }
                    )

                    HorizontalDivider(
                        color = LiteverTheme.colors.outlineVariant.copy(alpha = 0.3F),
                        modifier = Modifier.padding(horizontal = LiteverTheme.spacing.extraLarge)
                    )

                    ReMindSettingsItem(
                        title = stringResource(R.string.color_source_headline),
                        subtitle = currentColorPaletteLabel,
                        icon = ReMindIcons.Palette,
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(LiteverTheme.shapes.small)
                                        .background(currentColorSwatch)
                                )
                                Icon(
                                    imageVector = ReMindIcons.ChevronRight,
                                    contentDescription = null,
                                    tint = LiteverTheme.colors.onSurfaceVariant
                                )
                            }
                        },
                        onClick = { showColorPaletteDialog = true }
                    )
                }
            }

            // Language & Region Group
            item {
                ReMindSettingsGroup(title = stringResource(R.string.language_and_region_headline)) {
                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_language),
                        subtitle = currentLanguageLabel,
                        icon = ReMindIcons.Language,
                        trailingContent = {
                            Icon(
                                imageVector = ReMindIcons.ChevronRight,
                                contentDescription = null,
                                tint = LiteverTheme.colors.onSurfaceVariant
                            )
                        },
                        onClick = { showLanguageDialog = true }
                    )

                    HorizontalDivider(
                        color = LiteverTheme.colors.outlineVariant.copy(alpha = 0.3F),
                        modifier = Modifier.padding(horizontal = LiteverTheme.spacing.extraLarge)
                    )

                    ReMindSettingsItem(
                        title = stringResource(R.string.hour_format_24_headline),
                        subtitle = currentTimeFormatLabel,
                        icon = ReMindIcons.Schedule,
                        trailingContent = {
                            Icon(
                                imageVector = ReMindIcons.ChevronRight,
                                contentDescription = null,
                                tint = LiteverTheme.colors.onSurfaceVariant
                            )
                        },
                        onClick = { showTimeFormatDialog = true }
                    )
                }
            }
        }
    }

    if (showTimeFormatDialog) {
        SingleChoiceDialog(
            title = stringResource(R.string.hour_format_24_headline),
            options = timeOptions,
            selectedKey = uiState.timeFormat,
            onDismiss = { showTimeFormatDialog = false },
            onSelect = { key ->
                onTimeFormatChange(key)
                showTimeFormatDialog = false
            }
        )
    }

    if (showThemeModeDialog) {
        SingleChoiceDialog(
            title = stringResource(R.string.display_mode_headline),
            options = themeOptions,
            selectedKey = uiState.themeMode,
            onDismiss = { showThemeModeDialog = false },
            onSelect = { key ->
                onThemeModeChange(key)
                showThemeModeDialog = false
            }
        )
    }

    if (showLanguageDialog) {
        SingleChoiceDialog(
            title = stringResource(R.string.language_headline),
            options = languageOptions,
            selectedKey = uiState.language,
            onDismiss = { showLanguageDialog = false },
            onSelect = { key ->
                onLanguageChange(key)
                showLanguageDialog = false
            }
        )
    }

    if (showColorPaletteDialog) {
        ColorPaletteSelectionDialog(
            options = colorPaletteOptions,
            selectedKey = uiState.colorPalette,
            isDark = isDark,
            onDismiss = { showColorPaletteDialog = false },
            onSelect = { key ->
                onColorPaletteChange(key)
                showColorPaletteDialog = false
            }
        )
    }
}

@Composable
private fun SingleChoiceDialog(
    title: String,
    options: List<Pair<String, String>>,
    selectedKey: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(vn.io.litever.remind.core.designsystem.R.string.cancel))
            }
        },
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(key) }
                            .padding(vertical = LiteverTheme.spacing.smallMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = key == selectedKey,
                            onClick = { onSelect(key) }
                        )
                        Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))
                        Text(
                            text = label,
                            style = LiteverTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    )
}

private data class PaletteColorOption(
    val key: String,
    val titleRes: Int,
    val primaryLight: Color,
    val onPrimaryLight: Color,
    val primaryDark: Color,
    val onPrimaryDark: Color
)

@Composable
private fun ColorPaletteSelectionDialog(
    options: List<PaletteColorOption>,
    selectedKey: String,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val chunkedOptions = options.chunked(3)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(vn.io.litever.remind.core.designsystem.R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.color_source_headline)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = LiteverTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
            ) {
                chunkedOptions.forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        rowOptions.forEach { item ->
                            ColorCircleSwatch(
                                item = item,
                                isSelected = item.key == selectedKey,
                                isDark = isDark,
                                onClick = { onSelect(item.key) }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ColorCircleSwatch(
    item: PaletteColorOption,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = if (isDark) item.primaryDark else item.primaryLight
    val onPrimaryColor = if (isDark) item.onPrimaryDark else item.onPrimaryLight

    Column(
        modifier = modifier
            .width(64.dp)
            .clip(LiteverTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(vertical = LiteverTheme.spacing.extraSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.extraSmall)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            border = BorderStroke(2.5.dp, primaryColor),
                            shape = CircleShape
                        )
                    } else Modifier
                )
                .padding(if (isSelected) 4.dp else 0.dp)
                .clip(CircleShape)
                .background(primaryColor),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = ReMindIcons.Check,
                    contentDescription = null,
                    tint = onPrimaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = stringResource(item.titleRes),
            style = LiteverTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) primaryColor else LiteverTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun GeneralSettingsScreenPreview() {
    ReMindTheme {
        GeneralSettingsScreen(
            uiState = SettingsUiState(is24HourFormat = true, timeFormat = "SYSTEM", themeMode = "SYSTEM", colorPalette = "BLACK"),
            onTimeFormatChange = {},
            onThemeModeChange = {},
            onColorPaletteChange = {},
            onLanguageChange = {},
            onNavigateBack = {}
        )
    }
}










