package com.orange.ccmd.sandbox

import com.orange.ccmd.sandbox.routes.database
import com.orange.ccmd.sandbox.routes.strava
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.application
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.pipeline.PipelineContext
import io.ktor.routing.routing
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

val LOGGER: Logger = LoggerFactory.getLogger("App")

const val STRAVA_ROOT_URL = "https://www.strava.com/api/v3/"
const val PER_PAGE = 200

fun main(args: Array<String>): Unit = io.ktor.server.netty.DevelopmentEngine.main(args)

fun Application.module() {

    install(CallLogging) {
        level = Level.INFO
    }

    install(ContentNegotiation) {
        jackson { }
    }

    routing {
        strava()
        database()
    }
}

val client by lazy {
    HttpClient() {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }
}

fun PipelineContext<Unit, ApplicationCall>.tokenFromConf(): String {
    return application.environment.config.property("apiToken").getString()

}
