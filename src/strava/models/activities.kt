package com.orange.ccmd.sandbox.strava.models

import com.google.gson.annotations.SerializedName

data class Activity(
    val id: String,
    val name: String,
    val distance: Float,
    @SerializedName("moving_time") val movingTime: Int
)

data class ActivityDetails(
    val id: String,
    val name: String,
    val distance: Float,
    @SerializedName("moving_time") val movingTime: Int,
    @SerializedName("segment_efforts") val segmentEfforts: List<SegmentEffort>
)
