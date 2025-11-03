package org.ayal.commute_tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import org.ayal.commute_tracker.data.LocationRepository
import kotlinx.coroutines.Dispatchers

class SessionDetailViewModel(private val repository: LocationRepository) : ViewModel() {
    fun getSession(sessionId: Long) = liveData(Dispatchers.IO) {
        emit(repository.getSession(sessionId))
    }

    fun getTrackPoints(sessionId: Long) = liveData(Dispatchers.IO) {
        emit(repository.getTrackPointsForSession(sessionId))
    }
}
