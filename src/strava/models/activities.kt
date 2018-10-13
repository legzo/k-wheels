package com.orange.ccmd.sandbox.strava.models

import com.google.gson.annotations.SerializedName

data class Activity(
    val id: String,
    val name: String,
    val distance: Number,
    @SerializedName("moving_time") val movingTime: Number
)

data class ActivityDetails(
    val id: String,
    val name: String,
    val distance: Number,
    @SerializedName("moving_time") val movingTime: Number,
    @SerializedName("segment_efforts") val segmentEfforts: List<SegmentEffort>
)
