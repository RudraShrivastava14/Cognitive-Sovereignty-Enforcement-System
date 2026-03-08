package com.example.attentiontokenmanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider
import com.example.attentiontokenmanager.analytics.AnalyticsRepository
import com.example.attentiontokenmanager.analytics.AnalyticsScreen
import com.example.attentiontokenmanager.analytics.AnalyticsViewModel
import com.example.attentiontokenmanager.analytics.AnalyticsViewModelFactory
import com.example.attentiontokenmanager.ui.theme.AttentionTokenManagerTheme
import com.example.attentiontokenmanager.uii.TokenControlScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // 🔹 Create database
        val database = AppDatabase.getInstance(this)

        // 🔹 Create repository
        val repository = AnalyticsRepository(
            database.attentionEventDao()
        )

        // 🔹 Create ViewModel
        val factory = AnalyticsViewModelFactory(repository)

        val viewModel: AnalyticsViewModel =
            ViewModelProvider(this, factory)[AnalyticsViewModel::class.java]

        // 🔹 UI
        setContent {
            AttentionTokenManagerTheme {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // Token Control Section
                    TokenControlScreen()

                    HorizontalDivider()

                    // Analytics Section
                    AnalyticsScreen(viewModel)
                }
            }
        }

        checkNotificationPermissionAndStartService()
    }

    private fun checkNotificationPermissionAndStartService() {

        if (android.os.Build.VERSION.SDK_INT >= 33) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startForegroundService(
                    Intent(this, AttentionManagerService::class.java)
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }

        } else {
            // Android 12 and below don't need POST_NOTIFICATIONS permission
            startForegroundService(
                Intent(this, AttentionManagerService::class.java)
            )
        }
    }
    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startForegroundService(Intent(this, AttentionManagerService::class.java))
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        } else {
            startForegroundService(Intent(this, AttentionManagerService::class.java))
        }
    }
}

/* ===================================================== */
/* ================= PREVIEW SECTION =================== */
/* ===================================================== */

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AttentionTokenManagerTheme {
        Greeting("Android")
    }
}