package com.example.attentiontokenmanager.uii

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.attentiontokenmanager.*

@Composable
fun TokenControlScreen() {

    // 🔥 This forces recomposition when incremented
    var refreshTrigger by remember { mutableStateOf(0) }

    // 👇 READ it so Compose tracks it
    refreshTrigger

    val apps = listOf(
        AppTokenInfo("WhatsApp", "com.whatsapp"),
        AppTokenInfo("Gmail", "com.google.android.gm"),
        AppTokenInfo("Instagram", "com.instagram.android")
    )

    val manager = AttentionManagerService.instance

    Column(modifier = Modifier.padding(16.dp)) {

        Text(
            text = "Attention Token Control",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        apps.forEach { app ->

            val maxTokens = manager?.getMaxTokens(app.packageName) ?: 5
            var sliderValue by remember(app.packageName) {
                mutableStateOf(maxTokens.toFloat())
            }

            Text(text = app.name)

            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                    manager?.setMaxTokens(app.packageName, it.toInt())
                    refreshTrigger++   // 🔥 trigger UI refresh
                },
                valueRange = 0f..10f,
                steps = 9
            )

            Text(
                text = "Max tokens: ${sliderValue.toInt()} | Remaining: ${
                    manager?.getRemainingTokensPublic(app.packageName) ?: 0
                }"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = {
            manager?.resetAllTokens()
            refreshTrigger++   // 🔥 trigger UI refresh
        }) {
            Text("Reset All Tokens")
        }
    }
}
