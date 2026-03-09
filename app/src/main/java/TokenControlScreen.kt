package com.example.attentiontokenmanager.uii

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.attentiontokenmanager.AttentionManagerService

@Composable
fun TokenControlScreen(apps: List<AppInfo>) {

    val manager = AttentionManagerService.instance
    val refreshTrigger = remember { mutableStateOf(0) }

    Column(modifier = Modifier.padding(16.dp)) {

        apps.forEach { app ->

            val maxTokens = manager?.getMaxTokens(app.packageName) ?: 5

            var sliderValue by remember(app.packageName) {
                mutableStateOf(maxTokens.toFloat())
            }

            Text(text = app.name)

            Slider(
                value = sliderValue,
                valueRange = 0f..20f,
                steps = 19,
                onValueChange = {
                    sliderValue = it
                    manager?.setMaxTokens(app.packageName, it.toInt())
                    refreshTrigger.value++
                }
            )

            Text(
                text = "Remaining: ${
                    manager?.getRemainingTokensPublic(app.packageName) ?: 0
                }"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { manager?.resetAllTokens() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset All Tokens")
        }
    }
}