package com.example.attentiontokenmanager

/**
 * Used for analytics:
 * Blocked notification count per app
 */
data class AppCount(
    val packageName: String,
    val count: Int
)
