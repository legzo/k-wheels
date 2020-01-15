package com.orange.ccmd.sandbox.strava

import java.time.LocalDate
import java.time.ZoneOffset

private val Int.startAsEpoch: Long
    get() = LocalDate.of(this, 1, 1).atStartOfDay(ZoneOffset.UTC).toEpochSecond()

private val Int.endAsEpoch: Long
    get() = LocalDate.of(this, 12, 31).atStartOfDay(ZoneOffset.UTC).toEpochSecond()

data class StravaEndpoint(private val rootUrl: String) {
    fun forActivity(id: String) = "$rootUrl/activities/$id"
    fun forActivities(perPage: Number = 200, page: Number = 1, year: Int? = null): String {
        val url = "$rootUrl/activities?per_page=$perPage&page=$page"
        return if (year != null) {
            url.plus("&before=${year.endAsEpoch}&after=${year.startAsEpoch}")
        } else url
    }

    fun forToken() = "https://www.strava.com/oauth/token"
}
