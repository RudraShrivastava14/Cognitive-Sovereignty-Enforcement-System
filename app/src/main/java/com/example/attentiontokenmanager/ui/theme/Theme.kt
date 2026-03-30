package com.example.attentiontokenmanager.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ObsidianColorScheme = darkColorScheme(
    primary = MintPrimary,
    onPrimary = MintOnPrimary,
    primaryContainer = MintPrimaryContainer,
    background = ObsidianBackground,
    surface = ObsidianSurface,
    surfaceVariant = ObsidianSurfaceVariant,
    error = ErrorRed,
    onBackground = ObsidianTextPrimary,
    onSurface = ObsidianTextPrimary,
    onSurfaceVariant = ObsidianTextSecondary,
    outline = GhostBorder
)

@Composable
fun AttentionTokenManagerTheme(
    darkTheme: Boolean = true, // Force Dark-First Obsidian Terminal
    dynamicColor: Boolean = false, // Disable Google UI injection
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ObsidianColorScheme,
        typography = ObsidianTypography,
        content = content
    )
}