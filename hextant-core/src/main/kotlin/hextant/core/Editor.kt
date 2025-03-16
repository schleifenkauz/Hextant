/**
 * @author Nikolaus Knop
 */

package hextant.core

import hextant.context.Context
import hextant.core.editor.Expander
import hextant.serial.EditorAccessor
import hextant.serial.InvalidAccessorException
import reaktive.value.ReactiveValue

/**
 * An editor for results of type [R]
 */
interface Editor<out R> {
    val isInitialized: Boolean
    /**
     * A [reaktive.value.ReactiveValue] holding the result of compiling the content of the editor
     */
    val result: ReactiveValue<R>

    /**
     * The context of this editor
     */
    val context: Context

    /**
     * The parent of this Editor or `null` if this Editor is the root
     */
    val parent: Editor<*>?

    /**
     * @return the location of this editor relative its parent
     */
    val accessor: EditorAccessor?

    /**
     * The Expander that expanded this editor
     */
    val expander: Expander<*, *>?

    /**
     * The children of this editor
     */
    fun getChildren(): Collection<Editor<*>>

    /**
     * Initialize this editor in the given [context]
     */
    fun initialize(context: Context)

    /**
     * Locate this editor in the editor tree.
     */
    fun locate(parent: Editor<*>?, accessor: EditorAccessor, expander: Expander<*, *>? = null)

    /**
     * Return the child denoted by the given [accessor] or throw a [InvalidAccessorException] if there is no such child
     */
    fun getSubEditor(accessor: EditorAccessor): Editor<*>

    /**
     * Paste the given [editor] into this [Editor] if it is supported.
     * @return `true` only if pasting the given [editor] was successful.
     */
    fun paste(editor: Editor<*>): Boolean

    fun implCopy(): Editor<R> = throw UnsupportedOperationException("Copying ${javaClass.name} is not implemented")

    /**
     * Returns `true` only if this [Editor] supports copy/paste in principle.
     * The default implementation returns `false`.
     */
    fun supportsCopyPaste(): Boolean = false
}