package com.orange.ccmd.sandbox.database

import com.orange.ccmd.sandbox.models.ActivityDetailedStats
import com.orange.ccmd.sandbox.models.ActivityStats
import com.orange.ccmd.sandbox.models.EffortStats
import com.orange.ccmd.sandbox.models.SegmentData
import com.orange.ccmd.sandbox.models.SegmentStats
import com.orange.ccmd.sandbox.strava.models.Activity
import com.orange.ccmd.sandbox.strava.models.ActivityDetails
import com.orange.ccmd.sandbox.strava.models.TokenResponse
import org.dizitart.kno2.filters.eq
import org.dizitart.kno2.getRepository
import org.dizitart.kno2.nitrite
import org.dizitart.no2.objects.filters.ObjectFilters
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class DatabaseConnector(private val dbFile: String) {

    private val logger: Logger = LoggerFactory.getLogger("DatabaseConnector")

    private val db by lazy {
        nitrite {
            file = File(dbFile)
            autoCommitBufferSize = 2048
            compress = true
            autoCompact = false
        }
    }

    private val segmentDataRepo = db.getRepository<SegmentData>()
    private val activityRepo = db.getRepository<Activity>()
    private val tokenRepo = db.getRepository<TokenResponse>()

    fun getToken(): TokenResponse? {
        return tokenRepo.find().toList().firstOrNull()
    }

    fun updateToken(newToken: TokenResponse) {
        logger.info("Updating token with new value : $newToken")
        clearToken()
        tokenRepo.insert(newToken)
    }

    fun clearToken() {
        if (tokenRepo.find().totalCount() > 0) {
            tokenRepo.remove(ObjectFilters.ALL)
        }
    }

    fun saveActivities(activities: List<Activity>) {
        activityRepo.insert(activities.toTypedArray())
    }

    fun getAllActivities(): List<Activity> = activityRepo.find().sortedByDescending { it.startDate }.toList()

    fun getActivity(id: String) = activityRepo.find(Activity::id eq id).firstOrNull()

    fun getSegmentData(): List<SegmentData> = segmentDataRepo.find().toList()

    fun getSegmentData(id: String) = segmentDataRepo.find(SegmentData::id eq id).firstOrNull()

    fun clearSegmentData() {
        segmentDataRepo.remove(ObjectFilters.ALL)
    }

    fun clearActivities() {
        activityRepo.remove(ObjectFilters.ALL)
    }

    fun saveEfforts(activity: ActivityDetails) {
        activity.segmentEfforts?.forEach { effort ->
            val segmentData = getSegmentData(effort.segment.id)
            if (segmentData == null) {
                createSegmentData(effort.segment.id, effort.segment.name, activity.id, effort.elapsedTime)
            } else {
                segmentData.efforts[activity.id] = effort.elapsedTime
                updateSegmentData(segmentData)
            }
        }
    }

    private fun createSegmentData(id: String, name: String, activityId: String, elapsedTime: Float) {
        segmentDataRepo.insert(SegmentData(id, name, mutableMapOf(activityId to elapsedTime)))
    }

    private fun updateSegmentData(segmentData: SegmentData) {
        segmentDataRepo.update(SegmentData::id eq segmentData.id, segmentData)
    }

    fun getActivityStats(activity: ActivityDetails): ActivityStats? {
        if (activity.segmentEfforts == null) return null

        val stats = activity.segmentEfforts.map { effort ->
            val segmentData = getSegmentData(effort.segment.id)
            if (segmentData != null) {
                val time = effort.elapsedTime
                EffortStats(
                    effort.segment.id,
                    effort.segment.name,
                    segmentData.roundedPercentile(time),
                    segmentData.positionAsString(time),
                    segmentData.position(time),
                    segmentData.efforts.size
                )
            } else EffortStats(effort.segment.id, effort.segment.name, -1, "", -1, -1)
        }

        return ActivityStats(activity.id, activity.name, stats)
    }

    fun getActivityDetailedStats(activity: ActivityDetails): ActivityDetailedStats? {
        if (activity.segmentEfforts == null) return null

        val segments = activity.segmentEfforts
            .mapNotNull { effort ->
                val segmentData = getSegmentData(effort.segment.id)
                if (segmentData != null) {
                    val time = effort.elapsedTime
                    SegmentStats(
                        effort.segment.id,
                        effort.segment.name,
                        segmentData.roundedPercentile(time),
                        effort.elapsedTime,
                        segmentData.efforts.values.toList()
                    )
                } else null
            }

        return ActivityDetailedStats(
            activity.id,
            activity.startDate,
            activity.name,
            segments
        )
    }


    fun getActivitiesStats(activityDetails: List<ActivityDetails>): List<ActivityStats> {
        return activityDetails.mapNotNull { getActivityStats(it) }
    }

    fun deleteActivity(id: String): Boolean {
        return activityRepo.remove(Activity::id eq id).affectedCount > 0
    }
}
