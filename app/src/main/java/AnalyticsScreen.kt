package com.example.attentiontokenmanager.uii

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.attentiontokenmanager.*


import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

import androidx.compose.foundation.Canvas

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {

    val summary by viewModel.summary.collectAsState()
    val blockedApps by viewModel.blockedByApp.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAnalytics()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Attention Analytics",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Summary Card
        summary?.let {
            SummaryCard(it)
        } ?: Text("Loading analytics...")

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Blocked Notifications per App",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        blockedApps.forEach { app ->
            AppBlockRow(app)
        }
    }

    summary?.let {
        Spacer(modifier = Modifier.height(16.dp))
        AllowedBlockedPie(it)
    }
    Spacer(modifier = Modifier.height(24.dp))
    Text("Notification Timeline", style = MaterialTheme.typography.titleMedium)
    TimelineChart(hourlyEvents)

}

val hourlyEvents = listOf(
    0, 0, 1, 2, 4, 6,
    8, 7, 5, 3, 2, 1,
    0, 1, 3, 5, 6, 4,
    2, 1, 0, 0, 0, 0
)
@Composable
fun SummaryCard(summary: AnalyticsSummary) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("Total Notifications: ${summary.totalEvents}")
            Text("Blocked: ${summary.blockedEvents}")
            Text("Allowed: ${summary.allowedEvents}")

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = summary.blockRate.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Block Rate: ${(summary.blockRate * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}



@Composable
fun AppBlockRow(app: AppCount) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Text(app.packageName)

        LinearProgressIndicator(
            progress = (app.count / 20f).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        )

        Text(
            text = "Blocked: ${app.count}",
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun AllowedBlockedPie(summary: AnalyticsSummary) {

    val allowedAngle =
        if (summary.totalEvents == 0) 0f
        else (summary.allowedEvents.toFloat() / summary.totalEvents) * 360f

    Canvas(
        modifier = Modifier
            .size(180.dp)
            .padding(8.dp)
    ) {
        val diameter = size.minDimension
        val stroke = Stroke(width = diameter / 6)

        // Allowed (Green)
        drawArc(
            color = Color(0xFF4CAF50),
            startAngle = -90f,
            sweepAngle = allowedAngle,
            useCenter = false,
            style = stroke,
            size = Size(diameter, diameter)
        )

        // Blocked (Red)
        drawArc(
            color = Color(0xFFF44336),
            startAngle = -90f + allowedAngle,
            sweepAngle = 360f - allowedAngle,
            useCenter = false,
            style = stroke,
            size = Size(diameter, diameter)
        )
    }
}