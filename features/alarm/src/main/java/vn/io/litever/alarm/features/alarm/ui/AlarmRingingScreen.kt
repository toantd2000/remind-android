package vn.io.litever.alarm.features.alarm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.alarm.features.alarm.viewmodel.AlarmRingingViewModel

const val alarmRingingRoute = "alarm_ringing_route/{alarmId}"

@Composable
fun AlarmRingingRoute(
    alarmId: Long,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlarmRingingViewModel = hiltViewModel()
) {
    AlarmRingingScreen(
        alarmId = alarmId,
        onDismiss = {
            viewModel.dismissAlarm()
            onFinish()
        },
        onSnooze = {
            viewModel.snoozeAlarm()
            onFinish()
        },
        modifier = modifier
    )
}

@Composable
fun AlarmRingingScreen(
    alarmId: Long,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Báo Thức Trỗi Dậy!!", style = MaterialTheme.typography.displayMedium)
            Text("Mã báo thức: $alarmId", style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onSnooze) {
                    Text("Ngủ nướng (Snooze)")
                }
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("TẮT NGAY (Dismiss)")
                }
            }
        }
    }
}
