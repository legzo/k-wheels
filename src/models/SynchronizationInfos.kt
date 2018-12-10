package com.orange.ccmd.sandbox.models

data class SynchronizationInfos(
    val apiActivitiesCount: Int,
    val dbActivitiesCount: Int,
    val updated: List<ActivityStats>
)
