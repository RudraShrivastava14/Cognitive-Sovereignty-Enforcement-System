package com.example.attentiontokenmanager.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import com.example.attentiontokenmanager.uii.TimelineChart

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onRefresh: () -> Unit = {}
) {
    val summary by viewModel.summary.collectAsState()
    val blockedApps by viewModel.blockedByApp.collectAsState()
    val hourlyData by viewModel.eventsPerHour.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("TODAY", "THIS WEEK", "ALL TIME")

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
                IconButton(onClick = { onRefresh() }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // ── Tab Chips ──
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tabs.forEachIndexed { index, label ->
                        val isSelected = selectedTab == index
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.clickable { selectedTab = index }
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (summary == null) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Scanning telemetry...", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    return@Surface
                }

                val data = summary!!

                // ── Stat Cards (stacked vertically like Stitch) ──
                // Total Requests
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TOTAL REQUESTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                        Text(
                            String.format("%,d", data.totalEvents),
                            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ONLINE 24 HRS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 8.sp)
                        }
                    }
                }

                // Allowed Traffic
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ALLOWED TRAFFIC", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                        Text(
                            String.format("%,d", data.allowedEvents),
                            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("↑", color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${100 - (data.blockRate * 100).toInt()}% EFFICIENCY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 8.sp)
                        }
                    }
                }

                // Blocked Threats
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("BLOCKED THREATS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                        Text(
                            String.format("%,d", data.blockedEvents),
                            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("HIGH RISK MITIGATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontSize = 8.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Donut Chart ──
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("TRAFFIC DISTRIBUTION", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                        Text("Cognitive filtering by category", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))

                        val total = data.totalEvents
                        val allowedPct = if (total > 0) (data.allowedEvents.toFloat() / total * 100).toInt() else 0

                        Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                            val mintGreen = MaterialTheme.colorScheme.primary
                            val alertRed = MaterialTheme.colorScheme.error
                            Canvas(modifier = Modifier.size(140.dp)) {
                                val strokeWidth = 24f
                                val allowedSweep = if (total > 0) (data.allowedEvents.toFloat() / total) * 360f else 360f
                                val blockedSweep = if (total > 0) (data.blockedEvents.toFloat() / total) * 360f else 0f
                                drawArc(color = mintGreen, startAngle = -90f, sweepAngle = allowedSweep, useCenter = false, style = Stroke(strokeWidth, cap = StrokeCap.Butt))
                                drawArc(color = alertRed, startAngle = -90f + allowedSweep, sweepAngle = blockedSweep, useCenter = false, style = Stroke(strokeWidth, cap = StrokeCap.Butt))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${allowedPct}%", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = MaterialTheme.colorScheme.onBackground)
                                Text("SAFE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        // Legend
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PRODUCTIVITY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("${allowedPct}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BLOCKED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("${100 - allowedPct}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // ── Hourly Activity ──
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("HOURLY ACTIVITY", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                        Text("Real-time load balancing", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        TimelineChart(hourlyData)
                    }
                }

                // ── Restricted Entities ──
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("RESTRICTED ENTITIES", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (blockedApps.isEmpty()) {
                            Text("No blocked apps detected", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val max = blockedApps.maxOf { it.count }
                            blockedApps.take(5).forEach { app ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                                        Text("⚡", fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { if (max == 0) 0f else app.count / max.toFloat() },
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MaterialTheme.colorScheme.error,
                                            trackColor = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("${app.count}", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                    Text(" BLOCKED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontSize = 7.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}


