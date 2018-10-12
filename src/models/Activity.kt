package com.orange.ccmd.sandbox

import com.google.gson.annotations.SerializedName

data class Activity(
    val id: String,
    val name: String,
    val distance: Number,
    @SerializedName("moving_time") val movingTime: Number
)