package org.ayal.commute_tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import org.ayal.commute_tracker.databinding.ActivitySessionHistoryBinding

class SessionHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySessionHistoryBinding

    private val viewModel: SessionHistoryViewModel by viewModels {
        SessionHistoryViewModelFactory((application as CommuteTrackerApplication).locationRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sessionsRecyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = SessionHistoryAdapter(lifecycleScope, (application as CommuteTrackerApplication).locationRepository)
        binding.sessionsRecyclerView.adapter = adapter

        viewModel.sessions.observe(this) { sessions ->
            adapter.submitList(sessions)
        }
    }
}
