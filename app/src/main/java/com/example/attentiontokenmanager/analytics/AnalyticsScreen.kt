package com.example.attentiontokenmanager.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.min
import com.example.attentiontokenmanager.uii.TimelineChart

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel
) {
    val summary by viewModel.summary.collectAsState()
    val blockedApps by viewModel.blockedByApp.collectAsState()
    val hourlyData by viewModel.eventsPerHour.collectAsState()

    if (summary == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading analytics…")
        }
        return
    }

    val data = summary!!

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Attention Analytics",
            style = MaterialTheme.typography.headlineSmall
        )

        SummaryCard(data)

        Text(
            text = "Allowed vs Blocked",
            style = MaterialTheme.typography.titleMedium
        )
        AllowedBlockedPieChart(data)

        Text(
            text = "Activity Timeline (by hour)",
            style = MaterialTheme.typography.titleMedium
        )
        TimelineChart(hourlyData)

        Text(
            text = "Most Blocked Apps",
            style = MaterialTheme.typography.titleMedium
        )
        BlockedAppsBarChart(blockedApps)
    }
}

@Composable
private fun SummaryCard(summary: AnalyticsSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Total notifications: ${summary.totalEvents}")
            Text("Allowed: ${summary.allowedEvents}")
            Text("Blocked: ${summary.blockedEvents}")
        }
    }
}

@Composable
private fun AllowedBlockedPieChart(summary: AnalyticsSummary) {

    val total = summary.totalEvents
    if (total == 0) {
        Text("No data yet")
        return
    }

    val allowedSweep = (summary.allowedEvents / total.toFloat()) * 360f
    val blockedSweep = (summary.blockedEvents / total.toFloat()) * 360f

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val diameter = min(size.width, size.height)
        val topLeft = Offset(
            (size.width - diameter) / 2,
            (size.height - diameter) / 2
        )

        drawArc(
            color = Color(0xFF4CAF50),
            startAngle = 0f,
            sweepAngle = allowedSweep,
            useCenter = true,
            topLeft = topLeft,
            size = Size(diameter, diameter)
        )

        drawArc(
            color = Color(0xFFF44336),
            startAngle = allowedSweep,
            sweepAngle = blockedSweep,
            useCenter = true,
            topLeft = topLeft,
            size = Size(diameter, diameter)
        )
    }
}

@Composable
private fun BlockedAppsBarChart(apps: List<AppCount>) {

    if (apps.isEmpty()) {
        Text("No blocked apps 🎉")
        return
    }

    val max = apps.maxOf { it.count }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        apps.take(5).forEach { app ->

            Column {
                Text("${app.packageName} (${app.count})")

                // Use lambda form — the float-param overload is deprecated in Material3
                LinearProgressIndicator(
                    progress = { if (max == 0) 0f else app.count / max.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
