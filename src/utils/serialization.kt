package com.orange.ccmd.sandbox.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

object LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDateTime {
        val asString = json?.asJsonPrimitive?.asString ?: return LocalDateTime.now()
        return LocalDateTime.parse(asString)
    }
}

object StravaDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDateTime {
        val asString = json?.asJsonPrimitive?.asString ?: return LocalDateTime.now()

        return if (asString.startsWith("1")) {
            val epoch = asString.toLong()
            LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.UTC)
        } else ZonedDateTime.parse(asString).toLocalDateTime()
    }
}

object LocalDateTimeSerializer : JsonSerializer<LocalDateTime> {
    override fun serialize(
        src: LocalDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ) = JsonPrimitive(src.toString())
}
