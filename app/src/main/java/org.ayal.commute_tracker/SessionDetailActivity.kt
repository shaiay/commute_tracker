package org.ayal.commute_tracker

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import org.ayal.commute_tracker.R
import org.ayal.commute_tracker.data.TrackPoint
import org.ayal.commute_tracker.data.TrackingSession
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SessionDetailActivity : AppCompatActivity() {

    private val viewModel: SessionDetailViewModel by viewModels {
        SessionDetailViewModelFactory((application as CommuteTrackerApplication).locationRepository)
    }

    private lateinit var sessionNameTextView: TextView
    private lateinit var sessionNameEditText: EditText
    private lateinit var sessionDetailsTextView: TextView
    private lateinit var activityTypeSpinner: Spinner
    private lateinit var exportGpxButton: Button
    private lateinit var saveNameButton: Button
    private lateinit var saveActivityButton: Button

    private var session: TrackingSession? = null
    private var trackPoints: List<TrackPoint>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_detail)

        sessionNameTextView = findViewById(R.id.sessionNameTextView)
        sessionNameEditText = findViewById(R.id.sessionNameEditText)
        sessionDetailsTextView = findViewById(R.id.sessionDetailsTextView)
        activityTypeSpinner = findViewById(R.id.activityTypeSpinner)
        exportGpxButton = findViewById(R.id.exportGpxButton)
        saveNameButton = findViewById(R.id.saveNameButton)
        saveActivityButton = findViewById(R.id.saveActivityButton)

        val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1)
        if (sessionId != -1L) {
            viewModel.getSession(sessionId).observe(this) {
                session = it
                updateUi()
            }
            viewModel.getTrackPoints(sessionId).observe(this) {
                trackPoints = it
            }
        }

        exportGpxButton.setOnClickListener {
            exportGpx()
        }

        saveNameButton.setOnClickListener {
            session?.let {
                val newName = sessionNameEditText.text.toString()
                lifecycleScope.launch {
                    (application as CommuteTrackerApplication).locationRepository.updateSession(it.copy(name = newName))
                }
            }
        }

        saveActivityButton.setOnClickListener {
            session?.let {
                val newActivity = activityTypeSpinner.selectedItem.toString()
                lifecycleScope.launch {
                    (application as CommuteTrackerApplication).locationRepository.updateSession(it.copy(activityType = newActivity))
                }
            }
        }

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.activity_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        activityTypeSpinner.adapter = adapter
    }

    private fun updateUi() {
        session?.let {
            sessionNameTextView.text = it.name
            sessionNameEditText.setText(it.name)
            sessionDetailsTextView.text = "Activity: ${it.activityType}, Distance: ${it.distance}m"
            val activityTypes = resources.getStringArray(R.array.activity_types)
            val position = activityTypes.indexOf(it.activityType)
            if (position >= 0) {
                activityTypeSpinner.setSelection(position)
            }
        }
    }

    private fun exportGpx() {
        session?.let { s ->
            trackPoints?.let { tp ->
                val gpxContent = generateGpx(s, tp)
                val file = File(cacheDir, "${s.name}.gpx")
                FileOutputStream(file).use {
                    it.write(gpxContent.toByteArray())
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/gpx+xml"
                    putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Export GPX"))
            }
        }
    }

    private fun generateGpx(session: TrackingSession, trackPoints: List<TrackPoint>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<gpx version=\"1.1\" creator=\"Commute Tracker\">\n")
        sb.append("  <metadata>\n")
        sb.append("    <name>${session.name}</name>\n")
        sb.append("  </metadata>\n")
        sb.append("  <trk>\n")
        sb.append("    <name>${session.name}</name>\n")
        sb.append("    <trkseg>\n")
        trackPoints.forEach {
            sb.append("      <trkpt lat=\"${it.latitude}\" lon=\"${it.longitude}\">\n")
            sb.append("        <ele>${it.altitude}</ele>\n")
            sb.append("        <time>${formatTimestamp(it.timestamp)}</time>\n")
            sb.append("        <speed>${it.speed}</speed>\n")
            sb.append("        <course>${it.bearing}</course>\n")
            sb.append("      </trkpt>\n")
        }
        sb.append("    </trkseg>\n")
        sb.append("  </trk>\n")
        sb.append("</gpx>\n")
        return sb.toString()
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(timestamp))
    }

    companion object {
        const val EXTRA_SESSION_ID = "com.commute.tracker.SESSION_ID"
    }
}
