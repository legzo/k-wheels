package com.orange.ccmd.sandbox.strava

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.orange.ccmd.sandbox.database.DatabaseConnector
import com.orange.ccmd.sandbox.strava.models.Activity
import com.orange.ccmd.sandbox.strava.models.ActivityDetails
import com.orange.ccmd.sandbox.strava.models.TokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import org.apache.http.HttpHeaders
import org.apache.http.HttpHost
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class StravaConnector(
    private val endpoint: StravaEndpoint,
    private val proxyHost: String?,
    private val proxyPort: Int?,
    private val apiCode: String,
    private val apiClientId: String,
    private val apiClientSecret: String,
    private val database: DatabaseConnector
) {

    private val logger: Logger = LoggerFactory.getLogger("StravaConnector")
    private val perPage = 200
    private val maxPage = 20

    private suspend fun getToken(): TokenResponse {
        val tokenFromDB = database.getLastToken()
        logger.info("Access token from db : $tokenFromDB")

        return if (tokenFromDB == null) {
            val newTokenFromStrava = getNewAccessToken()
            logger.info("Storing token : $newTokenFromStrava")
            database.updateToken(newTokenFromStrava)
            newTokenFromStrava
        } else {
            if (tokenFromDB.isExpired()) {
                val refreshedToken = refresh(tokenFromDB)
                logger.info("Storing token : $refreshedToken")
                database.updateToken(refreshedToken)
                refreshedToken
            } else tokenFromDB
        }
    }

    private suspend fun getNewAccessToken(): TokenResponse {
        logger.info("Getting new token from strava")
        return client.post(endpoint.forToken()) {
            parameter("client_id", apiClientId)
            parameter("client_secret", apiClientSecret)
            parameter("code", apiCode)
            parameter("grant_type", "authorization_code")
        }
    }

    private suspend fun refresh(tokenFromDB: TokenResponse): TokenResponse {
        logger.info("Refreshing token")
        return client.post(endpoint.forToken()) {
            parameter("client_id", apiClientId)
            parameter("client_secret", apiClientSecret)
            parameter("refresh_token", tokenFromDB.refreshToken)
            parameter("grant_type", "refresh_token")
        }
    }

    suspend fun getAllActivities(year: Int? = null): List<Activity> {

        val allActivities = mutableListOf<Activity>()

        for (pageIndex in 1..maxPage) {
            val activities = getActivitiesPage(pageIndex, year)
            allActivities.addAll(activities)
            if (activities.size < perPage) break
        }

        return allActivities
    }

    suspend fun getActivity(id: String): ActivityDetails {
        val url = endpoint.forActivity(id)
        logger.info("Calling $url")
        return getWithToken(url)
    }

    private suspend inline fun <reified T> getWithToken(url: String): T {
        return client.get(url) {
            header(HttpHeaders.AUTHORIZATION, "Bearer ${getToken().accessToken}")
        }
    }

    private suspend fun getActivitiesPage(page: Number = 0, year: Int? = null): List<Activity> {
        val url = endpoint.forActivities(perPage, page, year)
        logger.info("Calling $url")
        return getWithToken(url)
    }

    private val client by lazy {
        HttpClient(Apache) {

            install(Logging) {
                level = LogLevel.INFO
            }

            expectSuccess = false

            install(JsonFeature) {
                serializer = GsonSerializer {
                    registerTypeAdapter(LocalDateTime::class.java, object : JsonDeserializer<LocalDateTime> {
                        override fun deserialize(
                            json: JsonElement?,
                            typeOfT: Type?,
                            context: JsonDeserializationContext?
                        ): LocalDateTime {
                            val asString = json?.asJsonPrimitive?.asString ?: return LocalDateTime.now()

                            return if (asString.startsWith("1")) {
                                val epoch = asString.toLong()
                                LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.UTC)
                            } else ZonedDateTime.parse(asString).toLocalDateTime()
                        }
                    })
                }
            }

            engine {

                customizeClient {
                    if (proxyHost != null && proxyPort != null) {
                        setProxy(HttpHost(proxyHost, proxyPort))
                    }
                }
            }
        }
    }
}
