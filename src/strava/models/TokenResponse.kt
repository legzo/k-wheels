package com.orange.ccmd.sandbox.strava.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_at") val expiresAt: LocalDateTime,
    @SerializedName("refresh_token") val refreshToken: String
) {
    fun isExpired() = LocalDateTime.now().isAfter(this.expiresAt.minusHours(6))
}
