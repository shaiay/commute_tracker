package org.ayal.commute_tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.ayal.commute_tracker.data.LocationRepository

class SessionHistoryViewModelFactory(private val repository: LocationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionHistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
