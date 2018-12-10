package com.orange.ccmd.sandbox.routes

import com.orange.ccmd.sandbox.database.DatabaseConnector
import com.orange.ccmd.sandbox.models.SynchronizationInfos
import com.orange.ccmd.sandbox.strava.StravaConnector
import com.orange.ccmd.sandbox.strava.models.Activity
import com.orange.ccmd.sandbox.strava.models.ActivityDetails
import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Route.dbRoutes(
    api: StravaConnector,
    database: DatabaseConnector
) {

    val logger: Logger = LoggerFactory.getLogger("DatabaseAPI")

    get("/db/syncActivities") {
        logger.info("Syncing activities from db")
        val apiActivities = api.getAllActivities()
        val dbActivities = database.getAllActivities()

        val activitiesToSync = apiActivities.minus(dbActivities)

        val updated = if (!activitiesToSync.isEmpty()) {
            database.saveActivities(activitiesToSync)
            val activityDetails = saveEffortsForActivities(activitiesToSync.map(Activity::id), api, database)

            database.getActivitiesStats(activityDetails)
        } else emptyList()

        call.respond(SynchronizationInfos(apiActivities.size, dbActivities.size, updated))
    }

    get("/db/activities") {
        logger.info("Getting activities from db")
        val activities = database.getAllActivities()
        logger.info("${activities.size} activites found, returning first ten")
        call.respond(activities.subList(0, 10))
    }

    get("/db/activities/{id}") {
        val id = call.parameters["id"].orEmpty()
        logger.info("Getting activity with id = $id from db")
        val activity = database.getActivity(id)
        if (activity != null) {
            logger.info("Activity found, returning it")
            call.respond(activity)
        } else call.respond(NotFound)
    }

    delete("/db/activities/{id}") {
        val id = call.parameters["id"].orEmpty()
        logger.info("Deleting activity with id = $id from db")
        if (database.deleteActivity(id)) call.respond(OK) else call.respond(NotFound)
    }

    get("/db/inject/{id}") {
        val id = call.parameters["id"].orEmpty()
        val activity = api.getActivity(id)
        database.saveEfforts(activity)
        call.respond(mapOf("injectedSegments" to activity.segmentEfforts.size))
    }

    get("/db/segments/{id}") {
        val id = call.parameters["id"].orEmpty()
        val segmentData = database.getSegmentData(id)
        if (segmentData != null) call.respond(segmentData) else call.respond(NotFound)
    }

    get("/db/segments") {
        call.respond(database.getSegmentData())
    }

    get("/db/segments/clear") {
        database.clearSegmentData()
        call.respond(mapOf("segments" to "cleared"))
    }
}

private suspend fun saveEffortsForActivities(
    ids: List<String>,
    api: StravaConnector,
    database: DatabaseConnector
): List<ActivityDetails> {
    val activityDetailsChunked = ids.chunked(20).map { chunkOfIds ->
        chunkOfIds.map { id -> GlobalScope.async { api.getActivity(id) } }
            .awaitAll()
    }

    val activityDetails = activityDetailsChunked.flatten()
    activityDetails.forEach(database::saveEfforts)
    return activityDetails
}
