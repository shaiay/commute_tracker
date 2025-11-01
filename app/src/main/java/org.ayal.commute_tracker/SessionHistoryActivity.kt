package org.ayal.commute_tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.ayal.commute_tracker.R

class SessionHistoryActivity : AppCompatActivity() {

    private val viewModel: SessionHistoryViewModel by viewModels {
        SessionHistoryViewModelFactory((application as CommuteTrackerApplication).locationRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_history)

        val recyclerView = findViewById<RecyclerView>(R.id.sessionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.sessions.observe(this) { sessions ->
            recyclerView.adapter = SessionHistoryAdapter(sessions)
        }
    }
}
