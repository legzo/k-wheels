package com.orange.ccmd.sandbox.routes

import com.orange.ccmd.sandbox.Activity
import com.orange.ccmd.sandbox.LOGGER
import com.orange.ccmd.sandbox.PER_PAGE
import com.orange.ccmd.sandbox.ROOT_URL
import com.orange.ccmd.sandbox.client
import com.orange.ccmd.sandbox.tokenFromConf
import io.ktor.application.call
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import org.dizitart.kno2.filters.eq
import org.dizitart.kno2.getRepository
import org.dizitart.kno2.nitrite
import java.io.File

fun Route.database() {

    get("/db/syncActivities") {
        LOGGER.info("Getting all activities")

        val activities = client.get<List<Activity>> {
            url("$ROOT_URL/activities?per_page=$PER_PAGE&access_token=${tokenFromConf()}")
        }

        LOGGER.info("${activities.size} activites found, returning first ten")

        val repository = db.getRepository<Activity> {
            insert(activities.toTypedArray())
        }

        call.respond(activities.subList(0, 10))
    }

    get("/db/activities") {
        LOGGER.info("Getting activities from db")

        val repository = db.getRepository<Activity>()
        val activities = repository.find().toList()

        LOGGER.info("${activities.size} activites found, returning first ten")

        call.respond(activities.subList(0, 10))
    }

    get("/db/activities/{id}") {
        val id = call.parameters["id"]
        LOGGER.info("Getting activity with id = $id from db")

        val repository = db.getRepository<Activity>()
        val activity = repository.find(Activity::id eq id).firstOrNull()

        if (activity != null) {
            LOGGER.info("Activity found, returning it")
            call.respond(activity)
        } else {
            call.respond(NotFound)
        }
    }

}

val db by lazy {
    nitrite {
        file = File("resources/strava.db")
        autoCommitBufferSize = 2048
        compress = true
        autoCompact = false
    }
}
