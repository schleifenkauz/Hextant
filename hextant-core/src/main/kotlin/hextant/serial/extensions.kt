/**
 * @author Nikolaus Knop
 */

package hextant.serial

import hextant.core.Editor
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.serializer
import java.io.File

/**
 * Return the first editor in the sequence of parents which has no parent.
 */
val Editor<*>.root: Editor<*>
    get() {
        var cur = this
        while (cur.parent != null) {
            cur = cur.parent!!
        }
        return cur
    }

fun <E: Editor<*>> E.reference(): EditorReference<E> {
    var cur: Editor<*> = this
    val accessorChain = mutableListOf<EditorAccessor>()
    while (cur.expander != null || cur.parent != null) {
        accessorChain.add(cur.accessor)
        cur = cur.expander ?: cur.parent!!
    }
    accessorChain.reverse()
    return EditorReference(cur, AccessorChain(accessorChain))
}

fun Editor<*>.parentChain(): Sequence<Editor<*>> = sequence {
    var cur = this@parentChain
    while (true) {
        cur = when {
            cur.expander != null -> cur.expander!!
            cur.parent != null -> cur.parent!!
            else -> break
        }
        yield(cur)
    }
}


/**
 * Encodes this [Editor] as JSON, and then writes it to the given [file],
 * such that it can be read again by [readEditorFromJson].
 */
@OptIn(ExperimentalSerializationApi::class)
fun Editor<*>.saveAsJson(file: File) {
    val stream = file.outputStream().buffered()
    json.encodeToStream(serialize(typeTag = true), stream)
    stream.close()
}

/**
 * Reconstructs an [Editor] from the given [file] that has been saved using [saveAsJson].
 */
@OptIn(ExperimentalSerializationApi::class)
fun readEditorFromJson(file: File): Editor<*> {
    val stream = file.inputStream().buffered()
    val editor = Editor.deserializeWithTypeTag(json.decodeFromStream<JsonElement>(stream))
    stream.close()
    return editor
}

/**
 * Encodes the given [value] as a JSON element and writes the string representation to this [File].
 */
fun <T> File.writeJson(serializer: SerializationStrategy<T>, value: T, json: Json = Json) {
    val txt = json.encodeToString(serializer, value)
    writeText(txt)
}

/**
 * Syntactic sugar for `[writeJson] (serializer<T>(), value)`
 */
inline fun <reified T> File.writeJson(value: T, json: Json = Json) {
    writeJson(serializer(), value, json)
}

/**
 * Reads the text from this [File], parses it as a JSON element and then reconstructs an object of type [T] from it.
 */
fun <T> File.readJson(deserializer: DeserializationStrategy<T>, json: Json = Json): T =
    json.decodeFromString(deserializer, readText())

/**
 * Syntactic sugar for `[readJson] (serializer<T>())`
 */
inline fun <reified T> File.readJson(json: Json = Json): T = readJson(serializer(), json)