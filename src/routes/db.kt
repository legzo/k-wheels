package com.orange.ccmd.sandbox.routes

import com.orange.ccmd.sandbox.database.DatabaseConnector
import com.orange.ccmd.sandbox.models.SynchronizationInfos
import com.orange.ccmd.sandbox.remote.RemoteDBConnector
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
import kotlin.math.min

fun Route.dbRoutes(
    api: StravaConnector,
    database: DatabaseConnector,
    remoteDBConnector: RemoteDBConnector
) {

    val logger: Logger = LoggerFactory.getLogger("DatabaseAPI")

    get("/db/syncActivities/{limit}") {
        val limit = call.parameters["limit"]
        if (limit != null) {
            logger.info("Syncing activities from db with a limit of $limit")
        } else {
            logger.info("Syncing activities from db with no limit")
        }

        val apiActivities = api.getAllActivities()
        val dbActivities = database.getAllActivities()

        val activitiesNotPresentLocally = apiActivities - dbActivities

        val activitiesToSync = if (limit != null) {
            val toIndex = min(limit.toInt(), activitiesNotPresentLocally.size)
            activitiesNotPresentLocally.subList(0, toIndex)
        } else activitiesNotPresentLocally

        val updated = if (activitiesToSync.isNotEmpty()) {
            database.saveActivities(activitiesToSync)
            logger.info("Fetching ${activitiesToSync.size} activities from Strava")
            val activityDetails = saveEffortsForActivities(activitiesToSync.map(Activity::id), api, database)

            database.getActivitiesStats(activityDetails)
        } else emptyList()

        call.respond(SynchronizationInfos(apiActivities.size, dbActivities.size, updated))
    }

    get("/db/activities") {
        logger.info("Getting activities from db")
        val activities = database.getAllActivities()
        logger.info("${activities.size} activities found, returning first ten")
        call.respond(activities.subList(0, 10))
    }

    delete("/db/activities") {
        logger.info("Clearing activities from db")
        database.clearActivities()
        call.respond(OK)
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
        call.respond(mapOf("injectedSegments" to (activity.segmentEfforts?.size ?: 0)))
    }

    get("/db/segments/{id}") {
        val id = call.parameters["id"].orEmpty()
        val segmentData = database.getSegmentData(id)
        if (segmentData != null) call.respond(segmentData) else call.respond(NotFound)
    }

    get("/db/segments") {
        call.respond(database.getSegmentData())
    }

    delete("/db/segments") {
        logger.info("Clearing segments")
        database.clearSegmentData()
        call.respond(mapOf("segments" to "cleared"))
    }

    get("/db/token/clear") {
        database.clearToken()
        call.respond(mapOf("token" to "cleared"))
    }

    get("/db/token/sync") {
        val remoteToken = remoteDBConnector.getRemoteToken()
        logger.info("Syncing token from remote, got : $remoteToken")
        database.updateToken(remoteToken)
        call.respond(remoteToken)
    }

    get("/db/token") {
        val token = database.getToken()
        if (token != null) call.respond(token)
        else call.respond(NotFound)
    }
}

private suspend fun saveEffortsForActivities(
    ids: List<String>,
    api: StravaConnector,
    database: DatabaseConnector
): List<ActivityDetails> {
    val activityDetailsChunked = ids
        .chunked(20)
        .map { chunkOfIds ->
            chunkOfIds
                .map { id -> GlobalScope.async { api.getActivity(id) } }
                .awaitAll()
        }

    val activityDetails = activityDetailsChunked.flatten()
    activityDetails.forEach(database::saveEfforts)
    return activityDetails
}
