package com.commute.tracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.commute.tracker.CommuteTrackerApplication
import com.commute.tracker.MainActivity
import com.commute.tracker.R
import com.commute.tracker.data.TrackPoint
import com.commute.tracker.data.TrackingSession
import com.commute.tracker.receiver.ActivityRecognitionReceiver
import com.google.android.gms.location.*
import kotlinx.coroutines.launch

class TrackingService : LifecycleService() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var locationCallback: LocationCallback
    private val repository by lazy { (application as CommuteTrackerApplication).locationRepository }

    private val activityRecognitionPendingIntent by lazy {
        val intent = Intent(this, ActivityRecognitionReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private var currentSession: TrackingSession? = null
    private val trackPoints = mutableListOf<TrackPoint>()

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        activityRecognitionClient = ActivityRecognition.getClient(this)
        createLocationCallback()
        isTracking.postValue(false)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                lifecycleScope.launch {
                    startTracking()
                }
            }
            ACTION_STOP_TRACKING -> {
                lifecycleScope.launch {
                    stopTracking()
                }
            }
        }
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
    private suspend fun startTracking() {
        isTracking.postValue(true)
        trackPoints.clear()
        val startTime = System.currentTimeMillis()
        val session = TrackingSession(
            name = "Commute on ${System.currentTimeMillis()}",
            startTime = startTime,
            endTime = startTime, // Will be updated when tracking stops
            distance = 0f,
            activityType = "driving"
        )
        val id = repository.insertSession(session)
        currentSession = session.copy(id = id)

        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
        activityRecognitionClient.requestActivityUpdates(30000, activityRecognitionPendingIntent)
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private suspend fun stopTracking() {
        isTracking.postValue(false)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        activityRecognitionClient.removeActivityUpdates(activityRecognitionPendingIntent)
        repository.insertTrackPoints(trackPoints)
        currentSession?.let {
            repository.updateSession(it.copy(endTime = System.currentTimeMillis()))
        }
        repository.groupSimilarSessions()
        stopForeground(true)
        stopSelf()
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    currentLocation.postValue(it)
                    val trackPoint = TrackPoint(
                        sessionId = currentSession?.id ?: 0,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        altitude = it.altitude,
                        speed = it.speed,
                        bearing = it.bearing,
                        timestamp = it.time
                    )
                    trackPoints.add(trackPoint)
                }
            }
        }
    }

    private fun createNotification(): android.app.Notification {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, TrackingService::class.java).apply {
            action = ACTION_STOP_TRACKING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Commute Tracker")
            .setContentText("Tracking your commute")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .addAction(R.mipmap.ic_launcher, "Stop", stopPendingIntent)
            .build()
    }

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val currentLocation = MutableLiveData<Location>()
        const val ACTION_START_TRACKING = "com.commute.tracker.START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.commute.tracker.STOP_TRACKING"
        private const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1
    }
}
