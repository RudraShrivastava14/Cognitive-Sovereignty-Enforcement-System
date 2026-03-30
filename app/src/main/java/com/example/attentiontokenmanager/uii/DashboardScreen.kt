package com.example.attentiontokenmanager.uii

import android.content.pm.PackageManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.attentiontokenmanager.AppDatabase
import com.example.attentiontokenmanager.AttentionManagerService
import kotlinx.coroutines.launch

fun getAppName(pm: PackageManager, packageName: String): String {
    return try {
        val info = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(info).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName.substringAfterLast('.')
    }
}

@Composable
fun DashboardScreen(database: AppDatabase, onRefresh: () -> Unit = {}) {

    val manager = AttentionManagerService.instance
    val context = LocalContext.current
    val pm = context.packageManager

    // Auto-updating list directly from the local DB!
    val appsFlow = remember(database) { database.appTokenDao().getAllTokensFlow() }
    val apps by appsFlow.collectAsState(initial = emptyList())

    // Calculate cognitive capacity dynamically 
    var totalRemaining = 0
    var totalMax = 0
    apps.forEach { app ->
        totalRemaining += app.remainingTokens
        totalMax += app.maxTokens
    }
    val capacityPercent = if (totalMax > 0) (totalRemaining * 100) / totalMax else 100

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "COGNITIVE SOVEREIGNTY",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onRefresh() }, modifier = Modifier.size(20.dp)) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // ── Cognitive Capacity ──
                Text(
                    "COGNITIVE CAPACITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "$capacityPercent",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 56.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        " / 100",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Normal,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Live Status Chip ──
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "LIVE OPTIMIZATION ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── Active Tokens Header ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ACTIVE TOKENS",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "GOTO CONTROL →",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (apps.isEmpty()) {
                    Text("No apps configured. Go to Control to track applications.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    // Render app cards dynamically in pairs
                    val chunkedApps = apps.chunked(2)
                    chunkedApps.forEach { rowApps ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowApps.forEach { app ->
                                val rem = app.remainingTokens
                                val max = app.maxTokens
                                val pct = if (max > 0) rem.toFloat() / max else 1f
                                val appName = getAppName(pm, app.packageName)

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Circular progress ring
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                                            val ringColor = if (pct > 0.3f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                            Canvas(modifier = Modifier.size(64.dp)) {
                                                drawArc(
                                                    color = Color(0xFF1C1C28),
                                                    startAngle = -90f,
                                                    sweepAngle = 360f,
                                                    useCenter = false,
                                                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                                                )
                                                drawArc(color = ringColor, startAngle = -90f, sweepAngle = 360f * pct, useCenter = false, style = Stroke(width = 6f, cap = StrokeCap.Round))
                                            }
                                            Text(
                                                "${(pct * 100).toInt()}%",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                color = ringColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            appName.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 1
                                        )
                                        Text(
                                            "$rem / $max",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            if (rowApps.size == 1) {
                                // Add empty weight for alignment
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Recent Activity (LIVE from DB) ──
                Text(
                    "RECENT ACTIVITY",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                val recentEvents by produceState(initialValue = emptyList<com.example.attentiontokenmanager.AttentionEventEntity>()) {
                    value = database.attentionEventDao().getRecentEvents(10)
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (recentEvents.isEmpty()) {
                            Text("No activity yet. Notifications will appear here as they are processed.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            recentEvents.forEach { event ->
                                val appName = getAppName(pm, event.packageName)
                                val wasBlocked = !event.allowed
                                val dotColor = if (wasBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                val label = if (wasBlocked) "BLOCKED" else "ALLOWED"
                                val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(event.timestamp))

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(6.dp).clip(CircleShape).background(dotColor)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(appName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
                                        Text("$label • $timeStr", style = MaterialTheme.typography.labelSmall, color = dotColor, fontSize = 8.sp)
                                    }
                                    Text(
                                        "${event.remainingTokens} left",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Reset All Tokens Button ──
                Button(
                    onClick = {
                        manager?.resetAllTokens()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7F1D1D),
                        contentColor = Color.White
                    )
                ) {
                    Text("⟳  RESET ALL TOKENS", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Clear Stale Data Button ──
                val coroutineScope = rememberCoroutineScope()
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            database.attentionEventDao().clearAllEvents()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("🗑 CLEAR ACTIVITY LOGS", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
