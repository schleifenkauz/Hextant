/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor
import hextant.serial.EditorAccessor
import kotlinx.serialization.json.JsonElement
import reaktive.value.ReactiveValue
import reaktive.value.binding.map

abstract class TransformedEditor<T, R>(internal val source: Editor<T>) : Editor<R> {
    protected abstract fun transform(result: T): R

    override val isInitialized: Boolean
        get() = source.isInitialized

    final override lateinit var result: ReactiveValue<R>
        private set

    override val context: Context
        get() = source.context

    override fun initialize(context: Context) {
        source.initialize(context)
        result = source.result.map(::transform)
    }

    override fun setupDefaultState() {
        source.setupDefaultState()
    }

    override val parent: Editor<*>?
        get() = source.parent

    override val accessor: EditorAccessor?
        get() = source.accessor

    override val expander: Expander<*, *>?
        get() = source.expander

    override fun getChildren(): Collection<Editor<*>> = source.getChildren()

    override fun paste(editor: Editor<*>): Boolean = source.paste(editor)

    override fun locate(parent: Editor<*>?, accessor: EditorAccessor, expander: Expander<*, *>?) {
        source.locate(parent, accessor)
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> = source.getSubEditor(accessor)

    override fun serialize(): JsonElement = source.serialize()

    override fun serialize(typeTag: Boolean): JsonElement = source.serialize(typeTag)

    override fun deserialize(element: JsonElement) {
        source.deserialize(element)
    }
}