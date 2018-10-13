package models

import com.google.gson.annotations.SerializedName

data class SegmentEffort(
    val segment: Segment,
    @SerializedName("elapsed_time") val elapsedTime: Number
)

data class Segment(val id: String, val name: String)
