package com.commute.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.commute.tracker.data.LocationRepository

class SessionHistoryViewModel(private val repository: LocationRepository) : ViewModel() {
    val sessions = liveData {
        emit(repository.getAllSessions())
    }
}
