package org.ayal.commute_tracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import org.ayal.commute_tracker.databinding.ActivityMainBinding
import org.ayal.commute_tracker.service.TrackingService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startStopButton.setOnClickListener {
            if (isTracking) {
                stopTracking()
            } else {
                startTracking()
            }
        }

        binding.historyButton.setOnClickListener {
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
            binding.speedTextView.text = "Speed: ${it.speed} m/s"
            binding.bearingTextView.text = "Bearing: ${it.bearing}°"
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
            binding.startStopButton.text = "Stop Tracking"
        } else {
            binding.startStopButton.text = "Start Tracking"
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(
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
