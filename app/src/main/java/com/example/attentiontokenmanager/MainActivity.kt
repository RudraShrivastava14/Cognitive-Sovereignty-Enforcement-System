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
import com.example.attentiontokenmanager.ui.theme.AttentionTokenManagerTheme
import com.example.attentiontokenmanager.uii.TokenControlScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // ✅ MUST be first

        enableEdgeToEdge()

        // 🔹 Runtime UI (WORKING)
        setContent {
            AttentionTokenManagerTheme {
                TokenControlScreen()
            }
        }

        // 🔹 Notification permission + service start (UNCHANGED)
        checkNotificationPermissionAndStartService()
    }

    private fun checkNotificationPermissionAndStartService() {
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
    }

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
            startForegroundService(
                Intent(this, AttentionManagerService::class.java)
            )
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
