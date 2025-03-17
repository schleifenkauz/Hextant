package hextant.serial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

class KJsonSerializer<T>(private val serializer: KSerializer<T>) : JsonSerializer<T> {
    override fun toJson(value: T): JsonElement = json.encodeToJsonElement(serializer, value)

    override fun fromJson(value: JsonElement): T = json.decodeFromJsonElement(serializer, value)

    companion object {
        inline fun <reified T> get() = KJsonSerializer<T>(serializer())
    }
}