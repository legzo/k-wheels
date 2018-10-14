package com.orange.ccmd.sandbox.models

data class ActivityStats(val id: String, val name: String, val efforts: List<EffortStats>)

data class EffortStats(val id: String, val name: String, val percentile: Number)
