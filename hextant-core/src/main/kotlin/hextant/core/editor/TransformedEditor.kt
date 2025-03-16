/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor
import hextant.serial.EditorAccessor
import kotlinx.serialization.Serializable
import reaktive.value.ReactiveValue
import reaktive.value.binding.map

@Serializable
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
}