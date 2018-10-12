package com.orange.ccmd.sandbox

import com.orange.ccmd.sandbox.routes.database
import com.orange.ccmd.sandbox.routes.strava
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.DevelopmentEngine.main(args)

fun Application.module() {

    val stravaEndpoint = StravaEndpoint(
        rootUrl = "https://www.strava.com/api/v3/",
        apiToken = environment.config.property("apiToken").getString()
    )

    install(CallLogging) {
        level = Level.INFO
    }

    install(ContentNegotiation) {
        jackson { }
    }

    routing {
        strava(stravaEndpoint)
        database(stravaEndpoint)
    }
}

val client by lazy {
    HttpClient {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }
}
