package com.example.attentiontokenmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val repository: AnalyticsRepository
) : ViewModel() {

    private val _summary = MutableStateFlow<AnalyticsSummary?>(null)
    val summary: StateFlow<AnalyticsSummary?> = _summary

    private val _blockedByApp = MutableStateFlow<List<AppCount>>(emptyList())
    val blockedByApp: StateFlow<List<AppCount>> = _blockedByApp

    fun loadAnalytics() {
        viewModelScope.launch {

            val total = repository.totalEvents()
            val blocked = repository.blockedEvents()
            val allowed = repository.allowedEvents()
            val rate = repository.blockRate()

            _summary.value = AnalyticsSummary(
                totalEvents = total,
                blockedEvents = blocked,
                allowedEvents = allowed,
                blockRate = rate
            )

            _blockedByApp.value = repository.blockedByApp()
        }
    }
}