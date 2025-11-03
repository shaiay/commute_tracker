package org.ayal.commute_tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SessionHistoryActivity : AppCompatActivity() {

    private val viewModel: SessionHistoryViewModel by viewModels {
        SessionHistoryViewModelFactory((application as CommuteTrackerApplication).locationRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_history)

        val recyclerView = findViewById<RecyclerView>(R.id.sessionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.sessions.observe(this) { _ ->
            recyclerView.adapter = SessionHistoryAdapter(lifecycleScope, (application as CommuteTrackerApplication).locationRepository)
        }
    }
}
