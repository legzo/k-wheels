package com.orange.ccmd.sandbox.strava.models

import com.orange.ccmd.sandbox.models.ActivityStats

data class SynchronizationInfos(
    val apiActivitiesCount: Int,
    val dbActivitiesCount: Int,
    val updated: List<ActivityStats>
)
