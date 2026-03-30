package com.example.attentiontokenmanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.example.attentiontokenmanager.analytics.*
import com.example.attentiontokenmanager.ui.theme.AttentionTokenManagerTheme
import com.example.attentiontokenmanager.uii.TokenControlScreen
import com.example.attentiontokenmanager.uii.DashboardScreen
import com.example.attentiontokenmanager.uii.CalendarScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val database = AppDatabase.getInstance(this)
        val repository = AnalyticsRepository(database.attentionEventDao())
        val factory = AnalyticsViewModelFactory(repository)
        val viewModel: AnalyticsViewModel =
            ViewModelProvider(this, factory)[AnalyticsViewModel::class.java]

        setContent {
            AttentionTokenManagerTheme {
                ObsidianLabApp(viewModel, database)
            }
        }

        checkAndStartService()
        scheduleDailyReset()
    }

    private fun checkAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startAttentionService()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        } else {
            startAttentionService()
        }
    }

    private fun startAttentionService() {
        val intent = Intent(this, AttentionManagerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun scheduleDailyReset() {
        val workRequest = PeriodicWorkRequestBuilder<TokenResetWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_token_reset",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startAttentionService()
        }
    }
}

enum class ObsidianTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Filled.Home),
    CONTROL("Control", Icons.Filled.Settings),
    ANALYTICS("Analytics", Icons.Filled.Home),  // placeholder, replaced in composable
    CALENDAR("Calendar", Icons.Filled.DateRange)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObsidianLabApp(viewModel: AnalyticsViewModel, database: AppDatabase) {
    var selectedTab by remember { mutableStateOf(ObsidianTab.DASHBOARD) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                ObsidianTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = {
                            Text(
                                text = tab.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                ObsidianTab.DASHBOARD -> DashboardScreen(database = database)
                ObsidianTab.CONTROL -> TokenControlScreen(database = database)
                ObsidianTab.ANALYTICS -> AnalyticsScreen(viewModel)
                ObsidianTab.CALENDAR -> CalendarScreen(database = database)
            }
        }
    }
}
