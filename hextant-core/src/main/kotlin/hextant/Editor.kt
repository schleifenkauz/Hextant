/**
 * @author Nikolaus Knop
 */

package hextant

import hextant.core.editor.Expander
import hextant.serial.EditorAccessor
import reaktive.collection.ReactiveCollection
import reaktive.value.ReactiveValue

/**
 * An editor for results of type [R]
 */
interface Editor<out R : Any> {
    /**
     * A [reaktive.value.ReactiveValue] holding the result of compiling the content of the editor
     */
    val result: EditorResult<R>

    /**
     * The parent of this Editor or `null` if this Editor is the root
     */
    val parent: ReactiveValue<Editor<*>?>

    /**
     * @return the location of this editor relative its parent
     */
    val accessor: EditorAccessor?

    /**
     * The children of this editor
     */
    val children: ReactiveCollection<Editor<*>>

    /**
     * The Expander that expanded this editor
     */
    val expander: ReactiveValue<Expander<*, *>?>

    /**
     * The context of this editor
     */
    val context: Context

    /**
     * This method should be considered an implementation detail.
     * It is likely to be removed soon and using it can cause all sorts of bugs.
     */
    @Deprecated("Treat as private")
    fun setParent(newParent: Editor<*>?)

    /**
     * This method should be considered an implementation detail.
     * It is likely to be removed soon and using it can cause all sorts of bugs.
     */
    @Deprecated("Treat as private")
    fun setExpander(newExpander: Expander<@UnsafeVariance R, *>?)

    @Deprecated("Treat as private")
    fun setAccessor(acc: EditorAccessor)

    /**
     * Copy this editor such that the new editor has the given editor.
     * The default implementation throws a [UnsupportedOperationException].
     * Implementing classes must ensure that this method is supported iff [supportsCopy] returns `true`.
     */
    fun copyForImpl(context: Context): Editor<R> = throw UnsupportedOperationException("Copying is not supported")

    /**
     * Return `true` iff this editor supports copying.
     * Default implementation returns `false`.
     */
    fun supportsCopy(): Boolean = false

    /**
     * Return the child denoted by the given [accessor] or throw a [NoSuchElementException] if there is no such child
     */
    fun getSubEditor(accessor: EditorAccessor): Editor<*>
}