/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor
import hextant.serial.EditorAccessor
import reaktive.value.ReactiveValue
import reaktive.value.binding.map

open class TransformedEditor<T, R>(
    internal val source: Editor<T>,
    private val transform: (T) -> R
) : Editor<R> {
    override val result: ReactiveValue<R> = source.result.map(transform)

    override val parent: Editor<*>?
        get() = source.parent
    override val accessor: EditorAccessor?
        get() = source.accessor

    override fun getChildren(): Collection<Editor<*>> = source.getChildren()
    override val expander: Expander<*, *>?
        get() = source.expander
    override val context: Context
        get() = source.context

    override fun paste(editor: Editor<*>): Boolean = source.paste(editor)

    override fun initialize(context: Context) {
        source.initialize(context)
    }

    override fun locate(parent: Editor<*>?, accessor: EditorAccessor, expander: Expander<*, *>?) {
        source.locate(parent, accessor)
    }

    override fun implCopy(): Editor<R> = TransformedEditor(source.snapshot(), transform)

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> = source.getSubEditor(accessor)
}