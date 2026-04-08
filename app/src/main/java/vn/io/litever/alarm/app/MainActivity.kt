package vn.io.litever.alarm.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import vn.io.litever.alarm.core.designsystem.theme.AlarmTheme
import vn.io.litever.alarm.features.alarm.ui.alarmGraph
import vn.io.litever.alarm.features.alarm.ui.alarmListRoute
import vn.io.litever.alarm.features.alarm.ui.alarmEditRoute

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLockScreenBypass()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleLockScreenBypass()
        
        enableEdgeToEdge()
        setContent {
            AlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Observe Global Ringing State
                    val alarmRingManager = dagger.hilt.EntryPoints.get(
                        applicationContext,
                        vn.io.litever.alarm.core.alarms.di.AlarmRingManagerEntryPoint::class.java
                    ).alarmRingManager()
                    val ringingAlarmId by alarmRingManager.ringingAlarmId.collectAsState()
                    
                    LaunchedEffect(ringingAlarmId) {
                        ringingAlarmId?.let { id ->
                            navController.navigate("alarm_ringing_route/$id") {
                                launchSingleTop = true
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = alarmListRoute
                    ) {
                        alarmGraph(
                            onNavigateToEdit = { id ->
                                navController.navigate("alarm_edit_route/$id")
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun handleLockScreenBypass() {
        val isRingingIntent = intent?.data?.toString()?.contains("alarm/ring") == true
        if (isRingingIntent) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
            }
        }
    }
}