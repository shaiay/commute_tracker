package org.ayal.commute_tracker

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.ayal.commute_tracker.data.LocationRepository
import org.ayal.commute_tracker.data.TrackingSession
import org.ayal.commute_tracker.databinding.ItemSessionBinding

class SessionHistoryAdapter(
    private val lifecycleScope: LifecycleCoroutineScope,
    private val repository: LocationRepository
) :
    ListAdapter<TrackingSession, SessionHistoryAdapter.ViewHolder>(SessionDiffCallback()) {

    class ViewHolder(val binding: ItemSessionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = getItem(position)
        holder.binding.sessionNameTextView.text = session.name
        val groupText = if (session.groupId != null) "Group: ${session.groupId}" else "No Group"
        holder.binding.sessionDetailsTextView.text = "Activity: ${session.activityType}, Distance: ${session.distance}m, $groupText"
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, SessionDetailActivity::class.java).apply {
                putExtra(SessionDetailActivity.EXTRA_SESSION_ID, session.id)
            }
            context.startActivity(intent)
        }

        if (session.groupId != null) {
            holder.binding.renameGroupButton.visibility = View.VISIBLE
            holder.binding.renameGroupButton.setOnClickListener {
                val context = holder.itemView.context
                val editText = android.widget.EditText(context)
                AlertDialog.Builder(context)
                    .setTitle("Rename Group")
                    .setView(editText)
                    .setPositiveButton("Save") { _, _ ->
                        val newName = editText.text.toString()
                        lifecycleScope.launch {
                            val group = repository.getGroup(session.groupId!!)
                            group?.let {
                                repository.updateGroup(it.copy(name = newName))
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        } else {
            holder.binding.renameGroupButton.visibility = View.GONE
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
