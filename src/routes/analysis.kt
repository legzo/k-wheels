package com.orange.ccmd.sandbox.routes

import com.orange.ccmd.sandbox.database.DatabaseConnector
import com.orange.ccmd.sandbox.strava.StravaConnector
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Route.analysis(
    api: StravaConnector,
    database: DatabaseConnector
) {

    val logger: Logger = LoggerFactory.getLogger("Analysis")

    get("/analysis/activities/{id}") {
        val id = call.parameters["id"].orEmpty()
        logger.info("Analysing activity with id = $id")

        val activity = api.getActivity(id)
        val activityStats = database.getActivityStats(activity)

        call.respond(activityStats)
    }
}
