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

    private val segmentDataRepo = db.getRepository<SegmentData>()
    private val activityRepo = db.getRepository<Activity>()

    fun updateActivities(activities: List<Activity>) {
        activityRepo.remove(ObjectFilters.ALL)
        activityRepo.insert(activities.toTypedArray())
    }

    fun getAllActivities(): List<Activity> = activityRepo.find().toList()

    fun getActivity(id: String) = activityRepo.find(Activity::id eq id).firstOrNull()

    fun getSegmentData(): List<SegmentData> = segmentDataRepo.find().toList()

    fun getSegmentData(id: String) = segmentDataRepo.find(SegmentData::id eq id).firstOrNull()

    fun clearSegmentData() {
        segmentDataRepo.remove(ObjectFilters.ALL)
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
        segmentDataRepo.insert(SegmentData(id, name, mutableMapOf(activityId to elapsedTime)))
    }

    private fun updateSegmentData(segmentData: SegmentData) {
        segmentDataRepo.update(SegmentData::id eq segmentData.id, segmentData)
    }
}