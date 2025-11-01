package org.ayal.commute_tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import org.ayal.commute_tracker.data.LocationRepository

class SessionHistoryViewModel(private val repository: LocationRepository) : ViewModel() {
    val sessions = liveData {
        emit(repository.getAllSessions())
    }
}
