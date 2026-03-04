package com.example.attentiontokenmanager.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel
) {
    val summary by viewModel.summary.collectAsState()
    val blockedApps by viewModel.blockedByApp.collectAsState()

    // Loading state
    if (summary == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading analytics…")
        }
        return
    }

    val data = summary!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Attention Analytics",
            style = MaterialTheme.typography.headlineSmall
        )

        // 1️⃣ Summary
        SummaryCard(data)

        // 2️⃣ Pie chart
        Text("Allowed vs Blocked")
        AllowedBlockedPieChart(data)

        // 3️⃣ Bar chart
        Text("Most Blocked Apps")
        BlockedAppsBarChart(blockedApps)
    }
}

/* ---------- Helper composables ---------- */

@Composable
private fun SummaryCard(summary: AnalyticsSummary) {
    Card {
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

        // Allowed
        drawArc(
            color = Color(0xFF4CAF50),
            startAngle = 0f,
            sweepAngle = allowedSweep,
            useCenter = true,
            topLeft = topLeft,
            size = Size(diameter, diameter)
        )

        // Blocked
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

                LinearProgressIndicator(
                    progress = app.count / max.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
