package com.orange.ccmd.sandbox.models

data class ActivityStats(
    val id: String,
    val name: String,
    val efforts: List<EffortStats>?
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
