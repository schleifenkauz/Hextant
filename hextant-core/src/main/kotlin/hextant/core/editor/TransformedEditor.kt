/**
 *@author Nikolaus Knop
 */

@file:Suppress("DEPRECATION")

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor
import hextant.serial.EditorAccessor
import hextant.serial.Snapshot
import hextant.serial.VirtualFile
import hextant.serial.snapshot
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import reaktive.collection.ReactiveCollection
import reaktive.value.ReactiveValue
import reaktive.value.binding.map

open class TransformedEditor<T, R>(
    internal val source: Editor<T>,
    transform: (T) -> R
) : Editor<R> {
    override val result: ReactiveValue<R> = source.result.map(transform)

    override fun createSnapshot(): Snapshot<*> = Snap()

    override val parent: Editor<*>?
        get() = source.parent
    override val accessor: ReactiveValue<EditorAccessor?>
        get() = source.accessor
    override val children: ReactiveCollection<Editor<*>>
        get() = source.children
    override val expander: Expander<*, *>?
        get() = source.expander
    override val context: Context
        get() = source.context
    override val file: VirtualFile<Editor<*>>?
        get() = source.file
    override val isRoot: Boolean
        get() = source.isRoot

    override fun initParent(parent: Editor<*>) {
        source.initParent(parent)
    }

    override fun initExpander(expander: Expander<*, *>) {
        source.initExpander(expander)
    }

    override fun setAccessor(acc: EditorAccessor) {
        source.setAccessor(acc)
    }

    override fun setFile(file: VirtualFile<Editor<*>>) {
        source.setFile(file)
    }

    override fun paste(snapshot: Snapshot<out Editor<*>>): Boolean = source.paste(snapshot)

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> = source.getSubEditor(accessor)

    private class Snap : Snapshot<TransformedEditor<*, *>>() {
        private lateinit var snapshot: Snapshot<Editor<*>>

        override fun doRecord(original: TransformedEditor<*, *>) {
            snapshot = original.source.snapshot()
        }

        override fun reconstructObject(original: TransformedEditor<*, *>) {
            snapshot.reconstructObject(original.source)
        }

        override fun encode(builder: JsonObjectBuilder) {
            builder.put("source", this.snapshot.encodeToJson())
        }

        override fun decode(element: JsonObject) {
            snapshot = decodeFromJson<Editor<*>>(element.getValue("source"))
        }
    }
}