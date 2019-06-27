/**
 * @author Nikolaus Knop
 */

package hextant

import hextant.core.editor.Expander
import reaktive.collection.ReactiveCollection
import reaktive.value.ReactiveValue

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
}