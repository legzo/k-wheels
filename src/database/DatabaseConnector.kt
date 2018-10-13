package com.orange.ccmd.sandbox.database

import com.orange.ccmd.sandbox.models.SegmentData
import com.orange.ccmd.sandbox.strava.models.Activity
import com.orange.ccmd.sandbox.strava.models.ActivityDetails
import org.dizitart.kno2.filters.eq
import org.dizitart.kno2.getRepository
import org.dizitart.kno2.nitrite
import org.dizitart.no2.objects.filters.ObjectFilters
import java.io.File

class DatabaseConnector(private val dbFile: String) {

    private val db by lazy {
        nitrite {
            file = File(dbFile)
            autoCommitBufferSize = 2048
            compress = true
            autoCompact = false
        }
    }

    private val segmentDataRepository = db.getRepository<SegmentData>()

    private val activityRepository = db.getRepository<Activity>()

    fun updateActivities(activities: List<Activity>) {
        activityRepository.remove(ObjectFilters.ALL)
        activityRepository.insert(activities.toTypedArray())
    }

    fun getAllActivities(): List<Activity> = activityRepository.find().toList()

    fun getActivity(id: String) = activityRepository.find(Activity::id eq id).firstOrNull()

    fun getSegmentData(): List<SegmentData> = segmentDataRepository.find().toList()

    fun getSegmentData(id: String) = segmentDataRepository.find(SegmentData::id eq id).firstOrNull()

    fun clearSegmentData() {
        segmentDataRepository.remove(ObjectFilters.ALL)
    }

    fun saveEfforts(activity: ActivityDetails) {
        activity.segmentEfforts.forEach { effort ->
            val segmentData = getSegmentData(effort.segment.id)
            if (segmentData == null) {
                createSegmentData(effort.segment.id, effort.segment.name, activity.id, effort.elapsedTime)
            } else {
                segmentData.efforts[activity.id] = effort.elapsedTime
                updateSegmentData(segmentData)
            }
        }
    }

    private fun createSegmentData(id: String, name: String, activityId: String, elapsedTime: Number) {
        segmentDataRepository.insert(SegmentData(id, name, mutableMapOf(activityId to elapsedTime)))
    }

    private fun updateSegmentData(segmentData: SegmentData) {
        segmentDataRepository.update(SegmentData::id eq segmentData.id, segmentData)
    }
}
