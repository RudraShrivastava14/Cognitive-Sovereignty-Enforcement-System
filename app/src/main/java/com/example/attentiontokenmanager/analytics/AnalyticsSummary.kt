package com.example.attentiontokenmanager.analytics

data class AnalyticsSummary(
    val totalEvents: Int,
    val blockedEvents: Int,
    val allowedEvents: Int,
    val blockRate: Float
)