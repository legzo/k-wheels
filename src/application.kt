package com.orange.ccmd.sandbox

import com.orange.ccmd.sandbox.database.DatabaseConnector
import com.orange.ccmd.sandbox.routes.analysis
import com.orange.ccmd.sandbox.routes.dbRoutes
import com.orange.ccmd.sandbox.routes.stravaRoutes
import com.orange.ccmd.sandbox.strava.StravaConnector
import com.orange.ccmd.sandbox.strava.StravaEndpoint
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import io.ktor.util.KtorExperimentalAPI
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = EngineMain.main(args)

@KtorExperimentalAPI
fun Application.module() {

    val stravaConnector = StravaConnector(
        StravaEndpoint(
            rootUrl = "https://www.strava.com/api/v3",
            apiToken = environment.config.property("apiToken").getString()
        ),
        proxyHost = environment.config.propertyOrNull("proxyHost")?.getString(),
        proxyPort = environment.config.propertyOrNull("proxyPort")?.getString()?.toInt(),
        apiCode = environment.config.propertyOrNull("apiCode")?.getString().orEmpty(),
        apiClientId = environment.config.propertyOrNull("apiClientId")?.getString().orEmpty(),
        apiClientSecret = environment.config.propertyOrNull("apiClientSecret")?.getString().orEmpty()
    )

    val dbConnector = DatabaseConnector("resources/strava.db")

    install(CallLogging) {
        level = Level.INFO
    }

    install(ContentNegotiation) {
        gson { }
    }

    routing {
        stravaRoutes(stravaConnector)
        dbRoutes(stravaConnector, dbConnector)
        analysis(stravaConnector, dbConnector)
    }
}
