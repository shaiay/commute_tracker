package com.commute.tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
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
        val adapter = SessionHistoryAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.sessions.observe(this) { sessions ->
            adapter.submitList(sessions)
        }
    }
}
