package org.ayal.commute_tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.ayal.commute_tracker.data.LocationRepository

class SessionDetailViewModelFactory(private val repository: LocationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
