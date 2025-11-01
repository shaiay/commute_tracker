package com.commute.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.commute.tracker.data.LocationRepository
import com.commute.tracker.data.TrackPoint
import com.commute.tracker.data.TrackingSession
import kotlinx.coroutines.Dispatchers

class SessionDetailViewModel(private val repository: LocationRepository) : ViewModel() {
    fun getSession(sessionId: Long) = liveData(Dispatchers.IO) {
        emit(repository.getSession(sessionId))
    }

    fun getTrackPoints(sessionId: Long) = liveData(Dispatchers.IO) {
        emit(repository.getTrackPointsForSession(sessionId))
    }
}
