package com.orange.ccmd.sandbox

import com.orange.ccmd.sandbox.database.DatabaseConnector
import com.orange.ccmd.sandbox.remote.RemoteDBConnector
import com.orange.ccmd.sandbox.routes.analysis
import com.orange.ccmd.sandbox.routes.dbRoutes
import com.orange.ccmd.sandbox.routes.stravaRoutes
import com.orange.ccmd.sandbox.strava.StravaConnector
import com.orange.ccmd.sandbox.strava.StravaEndpoint
import com.orange.ccmd.sandbox.utils.LocalDateTimeSerializer
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import io.ktor.util.KtorExperimentalAPI
import org.slf4j.event.Level
import java.time.LocalDateTime

fun main(args: Array<String>): Unit = EngineMain.main(args)

@KtorExperimentalAPI
fun Application.module() {

    val proxyHost = environment.config.propertyOrNull("proxyHost")?.getString()
    val proxyPort = environment.config.propertyOrNull("proxyPort")?.getString()?.toInt()

    val remoteDBConnector = RemoteDBConnector(
        endpoint = "http://localhost:8080",
        proxyHost = proxyHost,
        proxyPort = proxyPort
    )

    val dbConnector = DatabaseConnector("resources/strava.db")

    val stravaConnector = StravaConnector(
        StravaEndpoint(rootUrl = "https://www.strava.com/api/v3"),
        proxyHost = proxyHost,
        proxyPort = proxyPort,
        apiCode = environment.config.propertyOrNull("apiCode")?.getString().orEmpty(),
        apiClientId = environment.config.propertyOrNull("apiClientId")?.getString().orEmpty(),
        apiClientSecret = environment.config.propertyOrNull("apiClientSecret")?.getString().orEmpty(),
        database = dbConnector
    )

    install(CallLogging) {
        level = Level.INFO
    }

    install(ContentNegotiation) {
        gson {
            registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer)
        }
    }

    routing {
        stravaRoutes(stravaConnector)
        dbRoutes(stravaConnector, dbConnector, remoteDBConnector)
        analysis(stravaConnector, dbConnector)
    }
}
