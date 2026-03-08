package com.example.attentiontokenmanager.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AnalyticsViewModelFactory(
    private val repository: AnalyticsRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}