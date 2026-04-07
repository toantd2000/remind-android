package vn.io.litever.alarm.features.alarm.ringing

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vn.io.litever.alarm.core.designsystem.theme.AlarmTheme
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler.Companion.EXTRA_ALARM_ID

class RingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        turnScreenOnAndKeyguardOff()

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1)

        setContent {
            AlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    RingingScreen(
                        alarmId = alarmId,
                        onDismiss = {
                            stopService(Intent(this, AlarmService::class.java))
                            finish()
                        },
                        onSnooze = {
                            stopService(Intent(this, AlarmService::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }
}

@Composable
fun RingingScreen(
    alarmId: Long,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Báo Thức Hành Động!!", style = MaterialTheme.typography.displayLarge)
        Text("ID: $alarmId", style = MaterialTheme.typography.titleMedium)
        
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
