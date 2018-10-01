package com.orange.ccmd.sandbox.routes

import com.orange.ccmd.sandbox.Activity
import com.orange.ccmd.sandbox.LOGGER
import com.orange.ccmd.sandbox.PER_PAGE
import com.orange.ccmd.sandbox.ROOT_URL
import com.orange.ccmd.sandbox.client
import com.orange.ccmd.sandbox.tokenFromConf
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import kotlinx.coroutines.experimental.async

fun Route.strava() {

    get("/activities") {
        LOGGER.info("Getting all activities")

        val activities = client.get<List<Activity>> {
            url("$ROOT_URL/activities?per_page=$PER_PAGE&access_token=${tokenFromConf()}")
        }

        LOGGER.info("${activities.size} activites found, returning first ten")

        call.respond(activities.subList(0, 10))
    }

    get("/activities/{id}") {

        val id = call.parameters["id"]
        LOGGER.info("Getting activity with id = $id")

        val activity = getActivity(id)

        call.respond(activity)
    }

    get("/activities/all/{ids}") {

        val ids = call.parameters["ids"]
        LOGGER.info("Getting activities with ids = $ids")

        val activityIds = ids.orEmpty().split(",")
        val tasks = activityIds.map { id -> async { getActivity(id) } }
        val allActivities = tasks.forEach { task -> task.await() }

        call.respond(allActivities)
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.getActivity(id: String?): Activity {
    val activity = client.get<Activity> {
        url("$ROOT_URL/activities/$id?access_token=${tokenFromConf()}")
    }

    LOGGER.info(activity.toString())
    return activity
}