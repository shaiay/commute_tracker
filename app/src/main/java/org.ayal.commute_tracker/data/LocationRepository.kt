package org.ayal.commute_tracker.data

import android.content.Context
import androidx.room.Room

class LocationRepository(context: Context) {

    private val database = Room.databaseBuilder(
        context.applicationContext,
        CommuteDatabase::class.java, "commute-db"
    ).addMigrations(MIGRATION_1_2).build()

    private val trackingSessionDao = database.trackingSessionDao()
    private val trackPointDao = database.trackPointDao()
    private val sessionGroupDao = database.sessionGroupDao()

    suspend fun insertSession(session: TrackingSession) = trackingSessionDao.insert(session)

    suspend fun updateSession(session: TrackingSession) = trackingSessionDao.update(session)

    suspend fun insertTrackPoints(points: List<TrackPoint>) = trackPointDao.insertAll(points)

    suspend fun getSession(id: Long) = trackingSessionDao.getSession(id)

    suspend fun getAllSessions() = trackingSessionDao.getAllSessions()

    suspend fun getLatestSession() = trackingSessionDao.getLatestSession()

    suspend fun getTrackPointsForSession(sessionId: Long) = trackPointDao.getTrackPointsForSession(sessionId)

    suspend fun getGroup(id: Long) = sessionGroupDao.getGroup(id)

    suspend fun updateGroup(group: SessionGroup) = sessionGroupDao.update(group)

    suspend fun groupSimilarSessions() {
        val sessions = getAllSessions().filter { it.groupId == null }
        val groups = sessionGroupDao.getAllGroups().toMutableList()

        for (session in sessions) {
            val trackPoints = getTrackPointsForSession(session.id)
            if (trackPoints.size < 2) continue

            val startPoint = trackPoints.first()
            val endPoint = trackPoints.last()

            var foundGroup = false
            for (group in groups) {
                val groupSessions = getAllSessions().filter { it.groupId == group.id }
                if (groupSessions.isNotEmpty()) {
                    val representativeSession = groupSessions.first()
                    val representativeTrackPoints = getTrackPointsForSession(representativeSession.id)
                    if (representativeTrackPoints.size < 2) continue

                    val representativeStartPoint = representativeTrackPoints.first()
                    val representativeEndPoint = representativeTrackPoints.last()

                    val startDistance = distanceBetween(startPoint, representativeStartPoint)
                    val endDistance = distanceBetween(endPoint, representativeEndPoint)

                    if (startDistance <= 100 && endDistance <= 100) {
                        trackingSessionDao.update(session.copy(groupId = group.id))
                        foundGroup = true
                        break
                    }
                }
            }

            if (!foundGroup) {
                val newGroup = SessionGroup(name = "Group starting near (${startPoint.latitude}, ${startPoint.longitude})")
                val newGroupId = sessionGroupDao.insert(newGroup)
                trackingSessionDao.update(session.copy(groupId = newGroupId))
                groups.add(newGroup.copy(id = newGroupId))
            }
        }
    }

    private fun distanceBetween(p1: TrackPoint, p2: TrackPoint): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results)
        return results[0]
    }
}
