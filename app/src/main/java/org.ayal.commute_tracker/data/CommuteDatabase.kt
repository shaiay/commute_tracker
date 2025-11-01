package org.ayal.commute_tracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Database(entities = [TrackingSession::class, TrackPoint::class, SessionGroup::class], version = 2)
abstract class CommuteDatabase : RoomDatabase() {
    abstract fun trackingSessionDao(): TrackingSessionDao
    abstract fun trackPointDao(): TrackPointDao
    abstract fun sessionGroupDao(): SessionGroupDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `session_groups` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
        database.execSQL("ALTER TABLE `tracking_sessions` ADD COLUMN `groupId` INTEGER")
    }
}

@Dao
interface TrackingSessionDao {
    @Insert
    suspend fun insert(session: TrackingSession): Long

    @Query("SELECT * FROM tracking_sessions WHERE id = :id")
    suspend fun getSession(id: Long): TrackingSession?

    @Query("SELECT * FROM tracking_sessions ORDER BY startTime DESC")
    suspend fun getAllSessions(): List<TrackingSession>

    @Query("SELECT * FROM tracking_sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestSession(): TrackingSession?

    @Update
    suspend fun update(session: TrackingSession)
}

@Dao
interface TrackPointDao {
    @Insert
    suspend fun insertAll(points: List<TrackPoint>)

    @Query("SELECT * FROM track_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getTrackPointsForSession(sessionId: Long): List<TrackPoint>
}

@Dao
interface SessionGroupDao {
    @Insert
    suspend fun insert(group: SessionGroup): Long

    @Query("SELECT * FROM session_groups")
    suspend fun getAllGroups(): List<SessionGroup>

    @Query("SELECT * FROM session_groups WHERE id = :id")
    suspend fun getGroup(id: Long): SessionGroup?

    @Update
    suspend fun update(group: SessionGroup)
}
