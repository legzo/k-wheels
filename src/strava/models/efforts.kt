package com.orange.ccmd.sandbox.strava.models

import com.google.gson.annotations.SerializedName

data class SegmentEffort(
    val segment: Segment,
    @SerializedName("elapsed_time") val elapsedTime: Float
)

data class Segment(val id: String, val name: String)
