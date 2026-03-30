package com.example.attentiontokenmanager.uii

import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.attentiontokenmanager.AppDatabase
import com.example.attentiontokenmanager.AttentionManagerService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenControlScreen(database: AppDatabase, onRefresh: () -> Unit = {}) {

    val manager = AttentionManagerService.instance
    val context = LocalContext.current
    val pm = context.packageManager

    // Reactive UI from local DB!
    val appsFlow = remember(database) { database.appTokenDao().getAllTokensFlow() }
    val apps by appsFlow.collectAsState(initial = emptyList())

    if (manager == null) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("System Initializing...", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
        return
    }

    // Track which card is expanded
    var expandedIndex by remember { mutableStateOf(0) }
    
    // Track new app input
    var newAppPackage by remember { mutableStateOf("") }

    // Calculate global stats
    var globalUsed = 0
    var globalMax = 0
    apps.forEach { app ->
        globalUsed += (app.maxTokens) - (app.remainingTokens)
        globalMax += app.maxTokens
    }
    val aiEfficiency = if (globalMax > 0) ((globalMax - globalUsed).toFloat() / globalMax * 100) else 100f

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top Bar ──
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.weight(1f))
                Text("COGNITIVE SOVEREIGNTY", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.weight(1f))
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // ── Header ──
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SYSTEM LIVE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                Text("TOKEN CONTROL", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Calibrate cognitive resource allocation per application node. Adaptive AI will override if focus-drift is detected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Add App Card ──
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ADD APPLICATION PACKAGE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newAppPackage,
                                onValueChange = { newAppPackage = it },
                                placeholder = { Text("e.g. com.whatsapp", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f), fontSize = 12.sp) },
                                modifier = Modifier.weight(1f).height(50.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if(newAppPackage.isNotBlank()) {
                                        manager.setMaxTokens(newAppPackage, 5) // Default to 5
                                        newAppPackage = ""
                                    }
                                },
                                modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }

                // ── App Cards ──
                apps.forEachIndexed { index, app ->
                    val maxTokens = app.maxTokens
                    val remaining = app.remainingTokens
                    val isExpanded = expandedIndex == index

                    var sliderValue by remember(app.packageName, maxTokens) { mutableStateOf(maxTokens.toFloat()) }
                    var isAdaptive by remember(app.packageName, app.isAdaptiveLearningEnabled) { mutableStateOf(app.isAdaptiveLearningEnabled) }
                    val appName = getAppName(pm, app.packageName)

                    val statusText = when {
                        remaining <= 0 -> "LIMIT_REACHED"
                        remaining <= 2 -> "LOW_RESERVE"
                        else -> "NODE_ACTIVE"
                    }
                    val statusColor = when {
                        remaining <= 0 -> MaterialTheme.colorScheme.error
                        remaining <= 2 -> Color(0xFFFBBF24)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { expandedIndex = index }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // App header row
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                // Icon circle
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(appName.firstOrNull()?.toString() ?: "?", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(appName, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(statusColor))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(statusText, style = MaterialTheme.typography.labelSmall, color = statusColor, fontSize = 9.sp)
                                    }
                                }
                                // Token count
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${String.format("%02d", remaining)}/${String.format("%02d", maxTokens)}",
                                        fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                                        color = if (remaining <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                    Text("TOKENS ALLOCATED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
                                }
                            }

                            // Expanded content
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(16.dp))

                                // Token Density Slider
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("TOKEN DENSITY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        "${sliderValue.toInt()}.0",
                                        fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary, fontSize = 16.sp
                                    )
                                }
                                Slider(
                                    value = sliderValue,
                                    onValueChange = {
                                        sliderValue = it
                                        manager.setMaxTokens(app.packageName, it.toInt())
                                    },
                                    valueRange = 0f..10f, // Changed per user request: max capacity 10
                                    steps = 9,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text("MIN_RESERVE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("MAX_CAPACITY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Adaptive AI Control Card
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("ADAPTIVE AI CONTROL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground)
                                            Spacer(modifier = Modifier.weight(1f))
                                            Switch(
                                                checked = isAdaptive,
                                                onCheckedChange = {
                                                    isAdaptive = it
                                                    manager.updateTokenConfig(app.packageName, it, 0, 0)
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                                ),
                                                modifier = Modifier.height(24.dp)
                                            )
                                        }
                                        app.dailyAdjustmentLog?.let { log ->
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(verticalAlignment = Alignment.Top) {
                                                Box(modifier = Modifier.padding(top = 4.dp).size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("AI LOG: $log", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    TextButton(onClick = { manager.deleteTokenConfig(app.packageName) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Remove Node", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("REMOVE NODE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Bottom Stats ──
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GLOBAL LIMIT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                            Text(
                                "${globalMax - globalUsed} / $globalMax",
                                fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                                fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AI EFFICIENCY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                            Text(
                                String.format("%.1f%%", aiEfficiency),
                                fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                                fontSize = 18.sp, color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
