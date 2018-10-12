package com.orange.ccmd.sandbox.routes

import com.orange.ccmd.sandbox.Activity
import com.orange.ccmd.sandbox.StravaEndpoint
import com.orange.ccmd.sandbox.client
import io.ktor.application.call
import io.ktor.client.request.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.awaitAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Route.strava(endpoint: StravaEndpoint) {

    val logger: Logger = LoggerFactory.getLogger("StravaAPI")

    get("/activities") {
        logger.info("Getting all activities")

        val activities = client.get<List<Activity>>(endpoint.forActivities())

        logger.info("${activities.size} activites found, returning first ten")

        call.respond(activities.subList(0, 10))
    }

    get("/activities/{id}") {

        val id = call.parameters["id"].orEmpty()
        logger.info("Getting activity with id = $id")

        val activity = client.get<Activity>(endpoint.forActivity(id))

        call.respond(activity)
    }

    get("/activities/all/{ids}") {

        val ids = call.request.queryParameters["ids"].orEmpty()
        logger.info("Getting activities with ids = $ids")

        val activityIds = ids.split(",")
        val tasks = activityIds.map { id -> async { client.get<Activity>(endpoint.forActivity(id)) } }

        call.respond(tasks.awaitAll())
    }
}