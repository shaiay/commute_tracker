package org.ayal.commute_tracker

import android.app.Application
import org.ayal.commute_tracker.data.LocationRepository

class CommuteTrackerApplication : Application() {
    val locationRepository by lazy { LocationRepository(this) }
}
