package org.ayal.commute_tracker

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.ayal.commute_tracker.data.TrackingSession
import kotlinx.coroutines.launch
import org.ayal.commute_tracker.data.LocationRepository

class SessionHistoryAdapter(
    private val lifecycleScope: LifecycleCoroutineScope,
    private val repository: LocationRepository
) :
    ListAdapter<TrackingSession, SessionHistoryAdapter.ViewHolder>(SessionDiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sessionNameTextView: TextView = view.findViewById(R.id.sessionNameTextView)
        val sessionDetailsTextView: TextView = view.findViewById(R.id.sessionDetailsTextView)
        val renameGroupButton: Button = view.findViewById(R.id.renameGroupButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = getItem(position)
        holder.sessionNameTextView.text = session.name
        val groupText = if (session.groupId != null) "Group: ${session.groupId}" else "No Group"
        holder.sessionDetailsTextView.text = "Activity: ${session.activityType}, Distance: ${session.distance}m, $groupText"
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, SessionDetailActivity::class.java).apply {
                putExtra(SessionDetailActivity.EXTRA_SESSION_ID, session.id)
            }
            context.startActivity(intent)
        }

        if (session.groupId != null) {
            holder.renameGroupButton.visibility = View.VISIBLE
            holder.renameGroupButton.setOnClickListener {
                val context = holder.itemView.context
                val editText = EditText(context)
                AlertDialog.Builder(context)
                    .setTitle("Rename Group")
                    .setView(editText)
                    .setPositiveButton("Save") { _, _ ->
                        val newName = editText.text.toString()
                        lifecycleScope.launch {
                            val group = repository.getGroup(session.groupId)
                            group?.let {
                                repository.updateGroup(it.copy(name = newName))
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        } else {
            holder.renameGroupButton.visibility = View.GONE
        }
    }
}

class SessionDiffCallback : DiffUtil.ItemCallback<TrackingSession>() {
    override fun areItemsTheSame(oldItem: TrackingSession, newItem: TrackingSession): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TrackingSession, newItem: TrackingSession): Boolean {
        return oldItem == newItem
    }
}
