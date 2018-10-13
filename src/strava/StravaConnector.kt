package com.orange.ccmd.sandbox.strava

import com.orange.ccmd.sandbox.models.Activity
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get

class StravaConnector(private val endpoint: StravaEndpoint) {

    suspend fun getActivities(): List<Activity> {
        return client.get(endpoint.forActivities())
    }

    suspend fun getActivity(id: String): Activity {
        return client.get(endpoint.forActivity(id))
    }

    private val client by lazy {
        HttpClient {
            install(JsonFeature) {
                serializer = GsonSerializer()
            }
        }
    }
}
