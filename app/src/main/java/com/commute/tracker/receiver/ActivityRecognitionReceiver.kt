package com.commute.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.commute.tracker.CommuteTrackerApplication
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ActivityRecognitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            result?.let {
                val mostProbableActivity = it.mostProbableActivity
                if (mostProbableActivity.type != DetectedActivity.UNKNOWN && mostProbableActivity.confidence >= 75) {
                    val repository = (context.applicationContext as CommuteTrackerApplication).locationRepository
                    GlobalScope.launch {
                        val latestSession = repository.getLatestSession()
                        latestSession?.let {
                            repository.updateSession(it.copy(activityType = getActivityString(mostProbableActivity.type)))
                        }
                    }
                }
            }
        }
    }

    private fun getActivityString(type: Int): String {
        return when (type) {
            DetectedActivity.IN_VEHICLE -> "Driving"
            DetectedActivity.ON_BICYCLE -> "Cycling"
            DetectedActivity.ON_FOOT -> "Walking"
            DetectedActivity.RUNNING -> "Running"
            DetectedActivity.STILL -> "Still"
            DetectedActivity.TILTING -> "Tilting"
            DetectedActivity.WALKING -> "Walking"
            else -> "Unknown"
        }
    }
}
