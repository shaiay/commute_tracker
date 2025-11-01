package com.commute.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.commute.tracker.service.TrackingService

class TrackingControlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TrackingService.ACTION_START_TRACKING -> {
                Intent(context, TrackingService::class.java).also {
                    it.action = TrackingService.ACTION_START_TRACKING
                    context.startService(it)
                }
            }
            TrackingService.ACTION_STOP_TRACKING -> {
                Intent(context, TrackingService::class.java).also {
                    it.action = TrackingService.ACTION_STOP_TRACKING
                    context.startService(it)
                }
            }
        }
    }
}
