/**
 * @author Nikolaus Knop
 */

package hextant.serial

import hextant.context.Context
import hextant.context.withoutUndo
import hextant.core.Editor
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import reaktive.value.now
import java.io.File

/**
 * Return the first editor in the sequence of parents which is a root editor.
 */
val Editor<*>.root: Editor<*>
    get() {
        var cur = this
        while (!cur.isRoot) {
            cur = cur.parent ?: error("Editor has no root")
        }
        return cur
    }

/**
 * @return the location of this editor relative to its root
 */
val <E : Editor<*>> E.location: EditorLocation<E>
    get() {
        var cur: Editor<*> = this
        val accessors = mutableListOf<EditorAccessor>()
        while (true) {
            if (cur.isRoot) break
            while (cur.expander != null) {
                cur = cur.expander!!
                if (cur.isRoot) break
                accessors.add(ExpanderContent)
            }
            if (cur.isRoot) break
            val acc = cur.accessor.now ?: error("Editor has no accessor")
            cur = cur.parent ?: error("Editor has no parent")
            accessors.add(acc)
        }
        accessors.reverse()
        return AccessorChain(accessors)
    }

/**
 * Virtualize this editor
 */
fun <E : Editor<*>> E.virtualize(): VirtualEditor<E> = LocatedVirtualEditor(this, file!!, location)

/**
 * Makes this editor a root of the editor tree by assigning an [InMemoryFile]
 */
fun Editor<*>.makeRoot() {
    @Suppress("DEPRECATION")
    setFile(InMemoryFile(this))
}

/**
 * Makes a snapshot of this [Editor].
 */
@Suppress("UNCHECKED_CAST")
fun <T : SnapshotAware> T.snapshot(recordClass: Boolean = false): Snapshot<T> {
    val snapshot = createSnapshot() as Snapshot<T>
    snapshot.record(this, recordClass)
    return snapshot
}


/**
 * Makes a [Snapshot] of this [Editor], encodes it as JSON, and then writes it to the given [file],
 * such that it can be read again by [reconstructEditorFromJSONSnapshot].
 */
fun SnapshotAware.saveSnapshotAsJson(file: File) {
    val snapshot = snapshot(recordClass = true)
    val json = snapshot.encode()
    val txt = json.toString()
    file.writeText(txt)
}

/**
 * Reconstructs an [Editor] from the given [file] that has been saved using [saveSnapshotAsJson].
 * @param context the context with which the reconstructed editor is created
 */
fun reconstructEditorFromJSONSnapshot(file: File, context: Context): Editor<*> {
    val txt = file.readText()
    val json = Json.parseToJsonElement(txt)
    val snapshot = Snapshot.decode<Editor<*>>(json)
    return context.withoutUndo { snapshot.reconstructEditor(context) }
}

/**
 * Reconstruct an [Editor] from the given [Snapshot] using the given [context].
 */
fun <E : Editor<*>> Snapshot<E>.reconstructEditor(context: Context) =
    reconstruct(context) { cls -> cls.getConstructor(Context::class.java) }

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