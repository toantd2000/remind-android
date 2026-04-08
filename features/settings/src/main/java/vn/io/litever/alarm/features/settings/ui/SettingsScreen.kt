package vn.io.litever.alarm.features.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.material3.RadioButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.ui.draw.clip
import vn.io.litever.alarm.core.designsystem.theme.OceanBlueLight
import vn.io.litever.alarm.core.designsystem.theme.SunsetOrangeLight
import vn.io.litever.alarm.core.designsystem.theme.ForestGreenLight
import vn.io.litever.alarm.core.designsystem.theme.DeepIndigo

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        uiState = uiState,
        on24HourFormatChange = viewModel::set24HourFormat,
        onThemeModeChange = viewModel::setThemeMode,
        onColorPaletteChange = viewModel::setColorPalette
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    on24HourFormatChange: (Boolean) -> Unit,
    onThemeModeChange: (String) -> Unit,
    onColorPaletteChange: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cài đặt") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Column {
                    ListItem(
                        headlineContent = { Text("Định dạng 24 giờ") },
                        supportingContent = { Text("Hiển thị thời gian theo chuẩn 24h hoặc 12h") },
                        trailingContent = {
                            Switch(
                                checked = uiState.is24HourFormat,
                                onCheckedChange = on24HourFormatChange
                            )
                        }
                    )
                    
                    ListItem(
                        headlineContent = { Text("Giao diện (Theme)") },
                        supportingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = uiState.themeMode == "SYSTEM", onClick = { onThemeModeChange("SYSTEM") })
                                Text("Hệ thống")
                                Spacer(modifier = Modifier.width(8.dp))
                                RadioButton(selected = uiState.themeMode == "LIGHT", onClick = { onThemeModeChange("LIGHT") })
                                Text("Sáng")
                                Spacer(modifier = Modifier.width(8.dp))
                                RadioButton(selected = uiState.themeMode == "DARK", onClick = { onThemeModeChange("DARK") })
                                Text("Tối")
                            }
                        }
                    )
                    
                    ListItem(
                        headlineContent = { Text("Bảng màu (Palette)") },
                        supportingContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                ColorDot(
                                    color = Color.LightGray,
                                    selected = uiState.colorPalette == "DYNAMIC",
                                    isDynamic = true,
                                    onClick = { onColorPaletteChange("DYNAMIC") }
                                )
                                ColorDot(
                                    color = DeepIndigo,
                                    selected = uiState.colorPalette == "DEFAULT",
                                    onClick = { onColorPaletteChange("DEFAULT") }
                                )
                                ColorDot(
                                    color = OceanBlueLight,
                                    selected = uiState.colorPalette == "OCEAN",
                                    onClick = { onColorPaletteChange("OCEAN") }
                                )
                                ColorDot(
                                    color = SunsetOrangeLight,
                                    selected = uiState.colorPalette == "SUNSET",
                                    onClick = { onColorPaletteChange("SUNSET") }
                                )
                                ColorDot(
                                    color = ForestGreenLight,
                                    selected = uiState.colorPalette == "FOREST",
                                    onClick = { onColorPaletteChange("FOREST") }
                                )
                            }
                        }
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // TODO: Get real version dynamically
                Text(text = "Phiên bản 1.0 (1)")
            }
        }
    }
}

@Composable
fun ColorDot(
    color: Color,
    selected: Boolean,
    isDynamic: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) androidx.compose.material3.MaterialTheme.colorScheme.onSurface else Color.Transparent,
                shape = CircleShape
            )
    ) {
        if (isDynamic) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Wallpaper,
                contentDescription = "Dynamic Color",
                tint = if (selected) Color.White else Color.DarkGray,
                modifier = Modifier.align(Alignment.Center).size(20.dp)
            )
        } else if (selected) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    vn.io.litever.alarm.core.designsystem.theme.AlarmTheme {
        SettingsScreen(
            uiState = SettingsUiState(is24HourFormat = true, themeMode = "SYSTEM", colorPalette = "DEFAULT"),
            on24HourFormatChange = {},
            onThemeModeChange = {},
            onColorPaletteChange = {}
        )
    }
}
