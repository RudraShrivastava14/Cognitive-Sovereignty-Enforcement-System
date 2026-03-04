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
    val summary: StateFlow<AnalyticsSummary?> = _summary

    private val _blockedByApp =
        MutableStateFlow<List<AppCount>>(emptyList())
    val blockedByApp: StateFlow<List<AppCount>> = _blockedByApp

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            _summary.value = repository.loadSummary()
            _blockedByApp.value = repository.loadBlockedByApp()
        }
    }
}
