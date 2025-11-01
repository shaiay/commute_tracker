package com.commute.tracker

import android.app.Application
import com.commute.tracker.data.LocationRepository

class CommuteTrackerApplication : Application() {
    val locationRepository by lazy { LocationRepository(this) }
}
