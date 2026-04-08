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

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        uiState = uiState,
        on24HourFormatChange = viewModel::set24HourFormat,
        onThemeModeChange = viewModel::setThemeMode
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    on24HourFormatChange: (Boolean) -> Unit,
    onThemeModeChange: (String) -> Unit
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
