package org.ayal.commute_tracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import org.ayal.commute_tracker.service.TrackingService

class MainActivity : AppCompatActivity() {

    private lateinit var speedTextView: TextView
    private lateinit var bearingTextView: TextView
    private lateinit var startStopButton: Button
    private lateinit var historyButton: Button

    private var isTracking = false

    private val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.entries.all { it.value }) {
                // All permissions granted
            } else {
                // Some permissions denied
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        speedTextView = findViewById(R.id.speedTextView)
        bearingTextView = findViewById(R.id.bearingTextView)
        startStopButton = findViewById(R.id.startStopButton)
        historyButton = findViewById(R.id.historyButton)

        startStopButton.setOnClickListener {
            if (isTracking) {
                stopTracking()
            } else {
                startTracking()
            }
        }

        historyButton.setOnClickListener {
            startActivity(Intent(this, SessionHistoryActivity::class.java))
        }

        observeViewModel()
        requestPermissions()
    }

    private fun observeViewModel() {
        TrackingService.isTracking.observe(this) {
            isTracking = it
            updateUi()
        }

        TrackingService.currentLocation.observe(this) {
            speedTextView.text = "Speed: ${it.speed} m/s"
            bearingTextView.text = "Bearing: ${it.bearing}°"
        }
    }

    private fun startTracking() {
        Intent(this, TrackingService::class.java).also {
            it.action = TrackingService.ACTION_START_TRACKING
            startService(it)
        }
    }

    private fun stopTracking() {
        Intent(this, TrackingService::class.java).also {
            it.action = TrackingService.ACTION_STOP_TRACKING
            startService(it)
        }
    }

    private fun updateUi() {
        if (isTracking) {
            startStopButton.text = "Stop Tracking"
        } else {
            startStopButton.text = "Start Tracking"
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (true && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (true && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
