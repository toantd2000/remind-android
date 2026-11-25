package vn.io.litever.remind.features.settings.ui

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.brand.tokens.palettes.blackDarkColorScheme
import vn.io.litever.brand.tokens.palettes.blackLightColorScheme
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsCategory
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsDivider
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsItem
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.palettes.greenDarkColorScheme
import vn.io.litever.remind.core.designsystem.theme.palettes.greenLightColorScheme
import vn.io.litever.remind.core.designsystem.theme.palettes.orangeDarkColorScheme
import vn.io.litever.remind.core.designsystem.theme.palettes.orangeLightColorScheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.features.settings.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

@Composable
fun GeneralSettingsScreen(
    uiState: SettingsUiState,
    onTimeFormatChange: (String) -> Unit,
    onThemeModeChange: (String) -> Unit,
    onColorPaletteChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showLanguageDialog by remember { mutableStateOf(false) }

    val isDark = when (uiState.themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val effective24Hour = when (uiState.timeFormat) {
        "H12" -> false
        "H24" -> true
        else -> uiState.is24HourFormat
    }

    val context = LocalContext.current
    val dynamicLight = remember(context) { dynamicLightColorScheme(context) }
    val dynamicDark = remember(context) { dynamicDarkColorScheme(context) }

    val colorPaletteOptions = remember(dynamicLight, dynamicDark) {
        listOf(
            PaletteColorOption(
                key = "ORANGE",
                titleRes = R.string.color_orange,
                primaryLight = orangeLightColorScheme.primary,
                onPrimaryLight = orangeLightColorScheme.onPrimary,
                primaryContainerLight = orangeLightColorScheme.primaryContainer,
                onPrimaryContainerLight = orangeLightColorScheme.onPrimaryContainer,
                primaryDark = orangeDarkColorScheme.primary,
                onPrimaryDark = orangeDarkColorScheme.onPrimary,
                primaryContainerDark = orangeDarkColorScheme.primaryContainer,
                onPrimaryContainerDark = orangeDarkColorScheme.onPrimaryContainer
            ),
            PaletteColorOption(
                key = "GREEN",
                titleRes = R.string.color_green,
                primaryLight = greenLightColorScheme.primary,
                onPrimaryLight = greenLightColorScheme.onPrimary,
                primaryContainerLight = greenLightColorScheme.primaryContainer,
                onPrimaryContainerLight = greenLightColorScheme.onPrimaryContainer,
                primaryDark = greenDarkColorScheme.primary,
                onPrimaryDark = greenDarkColorScheme.onPrimary,
                primaryContainerDark = greenDarkColorScheme.primaryContainer,
                onPrimaryContainerDark = greenDarkColorScheme.onPrimaryContainer
            ),
            PaletteColorOption(
                key = "BLACK",
                titleRes = R.string.color_black,
                primaryLight = blackLightColorScheme.primary,
                onPrimaryLight = blackLightColorScheme.onPrimary,
                primaryContainerLight = blackLightColorScheme.primaryContainer,
                onPrimaryContainerLight = blackLightColorScheme.onPrimaryContainer,
                primaryDark = blackDarkColorScheme.primary,
                onPrimaryDark = blackDarkColorScheme.onPrimary,
                primaryContainerDark = blackDarkColorScheme.primaryContainer,
                onPrimaryContainerDark = blackDarkColorScheme.onPrimaryContainer
            ),
            PaletteColorOption(
                key = "DYNAMIC",
                titleRes = R.string.color_screen,
                primaryLight = dynamicLight.primary,
                onPrimaryLight = dynamicLight.onPrimary,
                primaryContainerLight = dynamicLight.primaryContainer,
                onPrimaryContainerLight = dynamicLight.onPrimaryContainer,
                primaryDark = dynamicDark.primary,
                onPrimaryDark = dynamicDark.onPrimary,
                primaryContainerDark = dynamicDark.primaryContainer,
                onPrimaryContainerDark = dynamicDark.onPrimaryContainer
            )
        )
    }

    val selectedColorOption = colorPaletteOptions.firstOrNull { it.key == uiState.colorPalette }
        ?: colorPaletteOptions.first()

    val themeOptions = listOf(
        ThemeModeOption(
            key = "SYSTEM",
            title = stringResource(R.string.theme_system),
            subtitle = stringResource(R.string.theme_system_desc),
            icon = ReMindIcons.BrightnessMedium
        ),
        ThemeModeOption(
            key = "LIGHT",
            title = stringResource(R.string.theme_light),
            subtitle = stringResource(R.string.theme_light_desc),
            icon = ReMindIcons.LightMode
        ),
        ThemeModeOption(
            key = "DARK",
            title = stringResource(R.string.theme_dark),
            subtitle = stringResource(R.string.theme_dark_desc),
            icon = ReMindIcons.DarkMode
        )
    )

    val languageOptions = listOf(
        "en" to stringResource(R.string.language_english),
        "vi" to stringResource(R.string.language_vietnamese)
    )
    val currentLanguageLabel = languageOptions.firstOrNull { it.first == uiState.language }?.second
        ?: stringResource(R.string.language_english)

    val formattedTimePreview = remember(effective24Hour) {
        val pattern = if (effective24Hour) "HH:mm" else "h:mm a"
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
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
            contentPadding = PaddingValues(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            // 1. Appearance Cards (System / Light / Dark)
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
                ) {
                    ReMindSettingsCategory(
                        title = stringResource(R.string.display_headline),
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraSmall)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        themeOptions.forEach { option ->
                            ThemeModeOptionCard(
                                option = option,
                                isSelected = option.key == uiState.themeMode,
                                onClick = { onThemeModeChange(option.key) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Primary Accent Theme Swatches
            item {
                ReMindSettingsGroup(title = stringResource(R.string.color_source_headline)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.spacing.medium,
                                end = MaterialTheme.spacing.medium,
                                top = MaterialTheme.spacing.small,
                                bottom = MaterialTheme.spacing.medium
                            ),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        colorPaletteOptions.forEach { item ->
                            ColorSwatchOptionCard(
                                item = item,
                                isSelected = item.key == selectedColorOption.key,
                                isDark = isDark,
                                onClick = { onColorPaletteChange(item.key) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. Time Format & Language
            item {
                ReMindSettingsGroup(title = stringResource(R.string.language_and_region_headline)) {
                    ReMindSettingsItem(
                        title = stringResource(R.string.hour_format_24_headline),
                        subtitle = stringResource(R.string.time_format_preview, formattedTimePreview),
                        icon = ReMindIcons.Schedule,
                        trailingContent = {
                            CompactTimeFormatToggle(
                                is24Hour = effective24Hour,
                                onSelect12Hour = { onTimeFormatChange("H12") },
                                onSelect24Hour = { onTimeFormatChange("H24") }
                            )
                        }
                    )

                    ReMindSettingsDivider()

                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_language),
                        subtitle = stringResource(R.string.setting_language_desc),
                        icon = ReMindIcons.Language,
                        onClick = { showLanguageDialog = true },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                            ) {
                                Text(
                                    text = currentLanguageLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = ReMindIcons.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
        }
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
}

@Composable
private fun CompactTimeFormatToggle(
    is24Hour: Boolean,
    onSelect12Hour: () -> Unit,
    onSelect24Hour: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .padding(MaterialTheme.spacing.extraSmall)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimeFormatSegment(
                label = stringResource(R.string.time_format_12h),
                selected = !is24Hour,
                onClick = onSelect12Hour
            )
            TimeFormatSegment(
                label = stringResource(R.string.time_format_24h),
                selected = is24Hour,
                onClick = onSelect24Hour
            )
        }
    }
}

@Composable
private fun TimeFormatSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val segmentShape = MaterialTheme.shapes.medium
    val contentPadding = PaddingValues(
        horizontal = MaterialTheme.spacing.none,
        vertical = MaterialTheme.spacing.none
    )

    if (selected) {
        Button(
            onClick = onClick,
            shape = segmentShape,
            contentPadding = contentPadding,
        ) {
            Text(
                text = label,
                maxLines = 1
            )
        }
    } else {
        TextButton(
            onClick = onClick,
            shape = segmentShape,
            contentPadding = contentPadding,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
        ) {
            Text(
                text = label,
                maxLines = 1
            )
        }
    }
}

private data class ThemeModeOption(
    val key: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
private fun ThemeModeOptionCard(
    option: ThemeModeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = if (isSelected) {
            null
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.small,
                    vertical = MaterialTheme.spacing.medium
                )
        ) {
            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = ReMindIcons.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Text(
                    text = option.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = option.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
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
                            .padding(vertical = MaterialTheme.spacing.smallMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = key == selectedKey,
                            onClick = { onSelect(key) }
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge
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
    val primaryContainerLight: Color,
    val onPrimaryContainerLight: Color,
    val primaryDark: Color,
    val onPrimaryDark: Color,
    val primaryContainerDark: Color,
    val onPrimaryContainerDark: Color
)

/**
 * Inline color swatch card displaying `primary` and `onPrimary` tokens clearly
 * so users can select a theme palette directly in General Settings without opening a dialog.
 */
@Composable
private fun ColorSwatchOptionCard(
    item: PaletteColorOption,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = if (isDark) item.primaryDark else item.primaryLight
    val onPrimaryColor = if (isDark) item.onPrimaryDark else item.onPrimaryLight
    val containerColor = if (isDark) item.primaryContainerDark else item.primaryContainerLight
    val onContainerColor = if (isDark) item.onPrimaryContainerDark else item.onPrimaryContainerLight

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) {
            containerColor
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = if (isSelected) {
            null
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.extraSmall,
                    vertical = MaterialTheme.spacing.smallMedium
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            // Dual-color swatch showcasing Primary (outer circle) and onPrimary (inner badge / check)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
                    .border(
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(onPrimaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ReMindIcons.Check,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    // Inner onPrimary dot so both Primary and onPrimary are always visible
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(onPrimaryColor)
                    )
                }
            }

            Text(
                text = stringResource(item.titleRes),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) onContainerColor else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GeneralSettingsScreenPreview() {
    ReMindTheme {
        GeneralSettingsScreen(
            uiState = SettingsUiState(
                is24HourFormat = false,
                timeFormat = "H12",
                themeMode = "SYSTEM",
                colorPalette = "ORANGE"
            ),
            onTimeFormatChange = {},
            onThemeModeChange = {},
            onColorPaletteChange = {},
            onLanguageChange = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF19120C)
@Composable
fun GeneralSettingsScreenDarkPreview() {
    ReMindTheme(darkTheme = true, colorPalette = "ORANGE") {
        GeneralSettingsScreen(
            uiState = SettingsUiState(
                is24HourFormat = true,
                timeFormat = "H24",
                themeMode = "DARK",
                colorPalette = "ORANGE"
            ),
            onTimeFormatChange = {},
            onThemeModeChange = {},
            onColorPaletteChange = {},
            onLanguageChange = {},
            onNavigateBack = {}
        )
    }
}
