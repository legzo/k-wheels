package com.orange.ccmd.sandbox.strava

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.orange.ccmd.sandbox.strava.models.Activity
import com.orange.ccmd.sandbox.strava.models.ActivityDetails
import com.orange.ccmd.sandbox.strava.models.TokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
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
import java.time.ZonedDateTime

class StravaConnector(
    private val endpoint: StravaEndpoint,
    private val proxyHost: String?,
    private val proxyPort: Int?,
    private val apiCode: String,
    private val apiClientId: String,
    private val apiClientSecret: String
) {

    private val logger: Logger = LoggerFactory.getLogger("StravaConnector")
    private val perPage = 200
    private val maxPage = 20
    private var cachedAccessToken: String? = null

    private suspend fun refreshToken(): String {
        logger.info("Getting new access token")

        val (accessToken) = client.post<TokenResponse>(endpoint.forToken()) {
            parameter("client_id", apiClientId)
            parameter("client_secret", apiClientSecret)
            parameter("code", apiCode)
            parameter("grant_type", "authorization_code")
        }

        logger.info("Storing token : $accessToken")
        cachedAccessToken = accessToken

        return accessToken
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
        if (cachedAccessToken == null) refreshToken()

        return client.get(url) {
            header(HttpHeaders.AUTHORIZATION, "Bearer $cachedAccessToken")
        }
    }

    private suspend fun getActivitiesPage(page: Number = 0, year: Int? = null): List<Activity> {
        val url = endpoint.forActivities(perPage, page, year)
        logger.info("Calling $url")
        return getWithToken(url)
    }

    private val client by lazy {
        HttpClient(Apache) {
            install(JsonFeature) {
                serializer = GsonSerializer {
                    registerTypeAdapter(LocalDateTime::class.java, object : JsonDeserializer<LocalDateTime> {
                        override fun deserialize(
                            json: JsonElement?,
                            typeOfT: Type?,
                            context: JsonDeserializationContext?
                        ): LocalDateTime {
                            return ZonedDateTime.parse(json?.asJsonPrimitive?.asString).toLocalDateTime()
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
