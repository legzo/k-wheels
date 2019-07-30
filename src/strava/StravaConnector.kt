package com.orange.ccmd.sandbox.strava

import com.orange.ccmd.sandbox.strava.models.Activity
import com.orange.ccmd.sandbox.strava.models.ActivityDetails
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class StravaConnector(private val endpoint: StravaEndpoint) {

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
        HttpClient {
            install(JsonFeature) {
                serializer = GsonSerializer()
            }
        }
    }
}
