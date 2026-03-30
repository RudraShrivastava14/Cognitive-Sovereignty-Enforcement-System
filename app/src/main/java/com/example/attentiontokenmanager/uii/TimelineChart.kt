package com.example.attentiontokenmanager.uii

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TimelineChart(eventsPerHour: List<Int>) {

    val max = eventsPerHour.maxOrNull()?.coerceAtLeast(1) ?: 1

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {

        val barWidth = size.width / eventsPerHour.size

        eventsPerHour.forEachIndexed { index, count ->
            val barHeight = (count.toFloat() / max) * size.height

            drawRect(
                color = Color(0xFF6EE7B7),
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = index * barWidth,
                    y = size.height - barHeight
                ),
                size = androidx.compose.ui.geometry.Size(
                    width = barWidth * 0.7f,
                    height = barHeight
                )
            )
        }
    }
}