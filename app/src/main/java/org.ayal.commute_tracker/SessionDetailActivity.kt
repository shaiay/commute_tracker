package org.ayal.commute_tracker

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.ayal.commute_tracker.data.TrackPoint
import org.ayal.commute_tracker.data.TrackingSession
import org.ayal.commute_tracker.databinding.ActivitySessionDetailBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SessionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySessionDetailBinding

    private val viewModel: SessionDetailViewModel by viewModels {
        SessionDetailViewModelFactory((application as CommuteTrackerApplication).locationRepository)
    }

    private var session: TrackingSession? = null
    private var trackPoints: List<TrackPoint>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1)
        if (sessionId != -1L) {
            viewModel.getSession(sessionId).observe(this) { sessionData ->
                session = sessionData
                updateUi()
            }
            viewModel.getTrackPoints(sessionId).observe(this) { trackPointsData ->
                trackPoints = trackPointsData
            }
        }

        binding.exportGpxButton.setOnClickListener {
            exportGpx()
        }

        binding.saveNameButton.setOnClickListener {
            session?.let {
                val newName = binding.sessionNameEditText.text.toString()
                lifecycleScope.launch {
                    (application as CommuteTrackerApplication).locationRepository.updateSession(it.copy(name = newName))
                }
            }
        }

        binding.saveActivityButton.setOnClickListener {
            session?.let {
                val newActivity = binding.activityTypeSpinner.selectedItem.toString()
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
        binding.activityTypeSpinner.adapter = adapter
    }

    private fun updateUi() {
        session?.let {
            binding.sessionNameTextView.text = it.name
            binding.sessionNameEditText.setText(it.name)
            binding.sessionDetailsTextView.text = "Activity: ${it.activityType}, Distance: ${it.distance}m"
            val activityTypes = resources.getStringArray(R.array.activity_types)
            val position = activityTypes.indexOf(it.activityType)
            if (position >= 0) {
                binding.activityTypeSpinner.setSelection(position)
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
        const val EXTRA_SESSION_ID = "org.ayal.commute_tracker.SESSION_ID"
    }
}
