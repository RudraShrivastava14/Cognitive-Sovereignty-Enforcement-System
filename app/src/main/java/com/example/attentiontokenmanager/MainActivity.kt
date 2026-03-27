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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.example.attentiontokenmanager.analytics.*
import com.example.attentiontokenmanager.ui.theme.AttentionTokenManagerTheme
import com.example.attentiontokenmanager.uii.TokenControlScreen

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
                // Scaffold handles edge-to-edge insets correctly
                Scaffold { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        TokenControlScreen()
                        HorizontalDivider()
                        AnalyticsScreen(viewModel)
                    }
                }
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
