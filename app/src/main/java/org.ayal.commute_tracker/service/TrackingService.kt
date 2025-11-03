package org.ayal.commute_tracker.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import org.ayal.commute_tracker.CommuteTrackerApplication
import org.ayal.commute_tracker.MainActivity
import org.ayal.commute_tracker.R
import org.ayal.commute_tracker.data.TrackPoint
import org.ayal.commute_tracker.data.TrackingSession
import org.ayal.commute_tracker.receiver.ActivityRecognitionReceiver

class TrackingService : LifecycleService() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var locationCallback: LocationCallback
    private val repository by lazy { (application as CommuteTrackerApplication).locationRepository }

    private val activityRecognitionPendingIntent by lazy {
        val intent = Intent(this, ActivityRecognitionReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
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

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        activityRecognitionClient.requestActivityUpdates(30000, activityRecognitionPendingIntent)
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private suspend fun stopTracking() {
        isTracking.postValue(false)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        activityRecognitionClient.removeActivityUpdates(activityRecognitionPendingIntent)
        repository.insertTrackPoints(trackPoints)
        currentSession?.let {
            repository.updateSession(it.copy(endTime = System.currentTimeMillis()))
        }
        repository.groupSimilarSessions()
        stopForeground(STOP_FOREGROUND_REMOVE)
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
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

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
        const val ACTION_START_TRACKING = "org.ayal.commute_tracker.START_TRACKING"
        const val ACTION_STOP_TRACKING = "org.ayal.commute_tracker.STOP_TRACKING"
        private const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1
    }
}
