package com.orange.ccmd.sandbox.strava

data class StravaEndpoint(private val rootUrl: String, private val apiToken: String) {

    fun forActivity(id: String) = "$rootUrl/activities/$id?access_token=$apiToken"
    fun forActivities(perPage: Number = 200) = "$rootUrl/activities?per_page=$perPage&access_token=$apiToken"

}
