package com.orange.ccmd.sandbox.strava.models

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String
)
