package corg.ayal.commute_tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import org.ayal.commute_tracker.data.LocationRepository
import org.ayal.commute_tracker.data.TrackPoint
import org.ayal.commute_tracker.data.TrackingSession
import kotlinx.coroutines.Dispatchers

class SessionDetailViewModel(private val repository: LocationRepository) : ViewModel() {
    fun getSession(sessionId: Long) = liveData(Dispatchers.IO) {
        emit(repository.getSession(sessionId))
    }

    fun getTrackPoints(sessionId: Long) = liveData(Dispatchers.IO) {
        emit(repository.getTrackPointsForSession(sessionId))
    }
}
