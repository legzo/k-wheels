package com.orange.ccmd.sandbox.models

import java.time.LocalDateTime
import java.util.SortedMap

data class ActivityDetailedStats(
    val id: String,
    val date: LocalDateTime,
    val name: String,
    val segments: List<SegmentStats>?
)

data class ActivityStats(
    val id: String,
    val name: String,
    val efforts: List<EffortStats>?
)

data class SegmentStats(
    val id: String,
    val name: String,
    val percentile: Number,
    val time: Float,
    val pastTimes: List<Float>
)

data class EffortStats(
    val id: String,
    val name: String,
    val percentile: Number,
    val positionAsString: String,
    val position: Int,
    val totalCount: Int
)

data class YearSummary(
    val commuteActivitiesCount: Int,
    val totalActivitiesCount: Int,
    val commuteDistance: Int,
    val totalDistance: Int,
    val distanceByMonth: Map<String, Int>,
    val distanceByMonthVisual: Map<String, String> = distanceByMonth
        .map { it.key to it.value.asVisual() }
        .toMap()
)

private fun Int.asVisual(): String {
    val width = this / 5
    return (0 until width).joinToString(separator = "") { "🁢" }
}
