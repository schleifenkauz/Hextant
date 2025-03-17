package hextant.serial

import kotlinx.serialization.json.JsonElement

interface JsonSerializer<T> {
    fun toJson(value: T): JsonElement
    fun fromJson(value: JsonElement): T
}