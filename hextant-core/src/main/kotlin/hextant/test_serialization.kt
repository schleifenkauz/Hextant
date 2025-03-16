package hextant

import javafx.scene.paint.Color
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

interface I<out T>

@Serializable
abstract class A<T> : I<T> {
    var v = 123

    @Transient
    var color = Color.CYAN

    @Transient
    var w = 0
}

@Serializable
class X(val x: Int = 0) : A<@Contextual Color>()

@Serializable
class Y : I<Color> {
    lateinit var x: I<@Contextual Color>
}

val module = SerializersModule {
    polymorphicDefaultSerializer(I::class) { v -> serializer(v::class, emptyList(), isNullable = false) }
    polymorphicDefaultDeserializer(I::class) { className ->
        val cls = Class.forName(className).kotlin
        serializer(cls, emptyList(), isNullable = false) as KSerializer<I<*>>
    }
}

val format = Json { serializersModule = module }

fun main() {
    val x: I<Any?> = X(x = 123).also { it.v = 23 }
    val y: I<Any?> = Y().also { it.x = x as X }
    println(format.encodeToString(x))
    println(format.encodeToString(y))
}