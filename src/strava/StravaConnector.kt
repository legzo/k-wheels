package com.orange.ccmd.sandbox.strava

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.orange.ccmd.sandbox.strava.models.Activity
import com.orange.ccmd.sandbox.strava.models.ActivityDetails
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import org.apache.http.HttpHost
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.ZonedDateTime

class StravaConnector(
    private val endpoint: StravaEndpoint,
    private val proxyHost: String?,
    private val proxyPort: Int?
) {

    private val logger: Logger = LoggerFactory.getLogger("StravaConnector")
    private val perPage = 200
    private val maxPage = 20

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
        return client.get(url)
    }

    private suspend fun getActivitiesPage(page: Number = 0, year: Int? = null): List<Activity> {
        val url = endpoint.forActivities(perPage, page, year)
        logger.info("Calling $url")
        return client.get(url)
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
