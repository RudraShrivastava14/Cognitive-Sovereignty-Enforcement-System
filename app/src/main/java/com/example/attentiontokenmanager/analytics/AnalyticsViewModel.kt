package com.example.attentiontokenmanager.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val repository: AnalyticsRepository
) : ViewModel() {

    private val _summary =
        MutableStateFlow<AnalyticsSummary?>(null)

    val summary: StateFlow<AnalyticsSummary?> =
        _summary


    private val _blockedByApp =
        MutableStateFlow<List<AppCount>>(emptyList())

    val blockedByApp: StateFlow<List<AppCount>> =
        _blockedByApp


    // 🔥 NEW: Hourly events state
    private val _eventsPerHour =
        MutableStateFlow<List<Int>>(List(24) { 0 })

    val eventsPerHour: StateFlow<List<Int>> =
        _eventsPerHour


    init {
        loadAnalytics()
    }


    private fun loadAnalytics() {

        viewModelScope.launch {

            try {

                val summaryResult =
                    repository.loadSummary()

                val blockedApps =
                    repository.loadBlockedByApp()

                val hourlyData =
                    repository.loadEventsPerHour()

                _summary.value = summaryResult
                _blockedByApp.value = blockedApps
                _eventsPerHour.value = hourlyData

            } catch (e: Exception) {

                // Prevent app crash if DB query fails
                _summary.value = AnalyticsSummary(
                    totalEvents = 0,
                    blockedEvents = 0,
                    allowedEvents = 0,
                    blockRate = 0f
                )

                _blockedByApp.value = emptyList()
                _eventsPerHour.value = List(24) { 0 }
            }
        }
    }
}