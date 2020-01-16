package com.orange.ccmd.sandbox.remote

import com.orange.ccmd.sandbox.strava.models.TokenResponse
import com.orange.ccmd.sandbox.utils.LocalDateTimeDeserializer
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import org.apache.http.HttpHost
import java.time.LocalDateTime

class RemoteDBConnector(
    private val endpoint: String,
    private val proxyHost: String?,
    private val proxyPort: Int?
) {

    suspend fun getRemoteToken(): TokenResponse {
        return client.get("$endpoint/db/token")
    }

    private val client by lazy {
        HttpClient(Apache) {

            install(Logging) {
                level = LogLevel.INFO
            }

            expectSuccess = false

            install(JsonFeature) {
                serializer = GsonSerializer {
                    registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer)
                }
            }

            engine {
                customizeClient {
                    if (proxyHost != null && proxyPort != null) {
                        setProxy(HttpHost(proxyHost, proxyPort))
                    }
                }
            }
        }
    }
}
