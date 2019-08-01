package com.orange.ccmd.sandbox.strava.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class Activity(
    val id: String,
    val name: String,
    @SerializedName("start_date_local") val startDate: LocalDateTime,
    val distance: Float,
    @SerializedName("moving_time") val movingTime: Int,
    val commute: Boolean
)

data class ActivityDetails(
    val id: String,
    val name: String,
    val distance: Float,
    @SerializedName("moving_time") val movingTime: Int,
    @SerializedName("segment_efforts") val segmentEfforts: List<SegmentEffort>
)

fun Float.toKm(): Double = this.toDouble() / 1000
