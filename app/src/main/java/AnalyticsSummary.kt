package com.example.attentiontokenmanager

data class AnalyticsSummary(
    val totalEvents: Int,
    val blockedEvents: Int,
    val allowedEvents: Int,
    val blockRate: Float
)