package com.orange.ccmd.sandbox

import io.ktor.application.*
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.pipeline.PipelineContext
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.coroutines.experimental.async
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

val LOGGER: Logger = LoggerFactory.getLogger("MyLogger")
const val ROOT_URL = "https://www.strava.com/api/v3/"
const val PER_PAGE = 200

fun main(args: Array<String>): Unit = io.ktor.server.netty.DevelopmentEngine.main(args)

fun Application.module() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        jackson {
        }
    }

    val client = HttpClient {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    routing {

        get("/activities") {
            LOGGER.info("Getting all activities")

            val activities = client.get<List<Activity>> {
                url("$ROOT_URL/activities?per_page=$PER_PAGE&access_token=${tokenFromConf()}")
            }

            LOGGER.info("${activities.size} activites found, displaying first ten")

            val firstTenActivities = activities.subList(0 , 10)
            call.respond(firstTenActivities)
        }

        get("/activities/{id}") {

            val id = call.parameters["id"]
            LOGGER.info("Getting activity with id = $id")

            val activity = getActivity(client, id)

            call.respond(activity)
        }

        get("/activities/all/{ids}") {

            val ids = call.parameters["ids"]
            LOGGER.info("Getting activities with ids = $ids")

            val activityIds = ids.orEmpty().split(",")
            val tasks = activityIds.map { activityId -> async { getActivity(client, activityId) }}
            val allActivities = tasks.forEach { task -> task.await() }

            call.respond(allActivities)
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.getActivity(client: HttpClient, id: String?): Activity {
    val activity = client.get<Activity> {
        url("$ROOT_URL/activities/$id?access_token=${tokenFromConf()}")
    }

    LOGGER.info(activity.toString())
    return activity
}

fun PipelineContext<Unit, ApplicationCall>.tokenFromConf(): String {
    return application.environment.config.property("apiToken").getString()

}
