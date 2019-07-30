package com.orange.ccmd.sandbox.routes

import com.orange.ccmd.sandbox.models.YearSummary
import com.orange.ccmd.sandbox.strava.StravaConnector
import com.orange.ccmd.sandbox.strava.models.Activity
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Route.stravaRoutes(api: StravaConnector) {

    val logger: Logger = LoggerFactory.getLogger("StravaRoute")

    get("/activities") {
        logger.info("Getting all activities")
        val activities = api.getAllActivities()
        logger.info("${activities.size} activites found, returning first ten")
        call.respond(activities.subList(0, 10))
    }

    get("/activities/{id}") {
        val id = call.parameters["id"].orEmpty()
        logger.info("Getting activity with id = $id")
        val activity = api.getActivity(id)
        call.respond(activity)
    }

    get("/activities/all/{ids}") {
        val ids = call.request.queryParameters["ids"].orEmpty()
        logger.info("Getting activities with ids = $ids")
        val activityIds = ids.split(",")
        val tasks = activityIds.map { id -> async { api.getActivity(id) } }
        call.respond(tasks.awaitAll())
    }

    get("/activities/commute/{year}") {
        val year = call.parameters["year"].orEmpty().toInt()
        logger.info("Getting activities for year = $year")

        val allActivitiesForYear = api.getAllActivities(year)
        val commuteActivitiesForYear = allActivitiesForYear.filter { it.commute }

        fun distanceToKm(it: Activity) = it.distance.toInt() / 1000

        call.respond(
            YearSummary(
                numberOfCommuteActivities = commuteActivitiesForYear.size,
                numberOfActivities = allActivitiesForYear.size,
                distanceForCommute = commuteActivitiesForYear.sumBy(::distanceToKm),
                totalDistance = allActivitiesForYear.sumBy(::distanceToKm)
            )
        )
    }
}
