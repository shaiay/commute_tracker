package com.commute.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_groups")
data class SessionGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
