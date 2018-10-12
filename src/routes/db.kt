package com.orange.ccmd.sandbox.routes

import com.orange.ccmd.sandbox.StravaEndpoint
import com.orange.ccmd.sandbox.client
import com.orange.ccmd.sandbox.models.Activity
import io.ktor.application.call
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import org.dizitart.kno2.filters.eq
import org.dizitart.kno2.getRepository
import org.dizitart.kno2.nitrite
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

fun Route.database(endpoint: StravaEndpoint) {

    val logger: Logger = LoggerFactory.getLogger("DatabaseAPI")

    val repository by lazy {
        nitrite {
            file = File("resources/strava.db")
            autoCommitBufferSize = 2048
            compress = true
            autoCompact = false
        }.getRepository<Activity>()
    }

    get("/db/syncActivities") {
        logger.info("Getting all activities")

        val activities = client.get<List<Activity>>(endpoint.forActivities())

        logger.info("${activities.size} activites found, returning first ten")

        repository.insert(activities.toTypedArray())

        call.respond(activities.subList(0, 10))
    }

    get("/db/activities") {
        logger.info("Getting activities from db")

        val activities = repository.find().toList()

        logger.info("${activities.size} activites found, returning first ten")

        call.respond(activities.subList(0, 10))
    }

    get("/db/activities/{id}") {
        val id = call.parameters["id"]
        logger.info("Getting activity with id = $id from db")

        val activity = repository.find(Activity::id eq id).firstOrNull()

        if (activity != null) {
            logger.info("Activity found, returning it")
            call.respond(activity)
        } else {
            call.respond(NotFound)
        }
    }
}
