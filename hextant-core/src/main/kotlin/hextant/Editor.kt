/**
 * @author Nikolaus Knop
 */

package hextant

import hextant.core.editor.Expander
import hextant.project.editor.FileEditor
import hextant.serial.EditorAccessor
import hextant.serial.InvalidAccessorException
import reaktive.collection.ReactiveCollection

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
    val parent: Editor<*>?

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
    val expander: Expander<*, *>?

    /**
     * The context of this editor
     */
    val context: Context

    /**
     * The file of this editor
     */
    val file: FileEditor<*>?

    /**
     * Returns `true` only if this editor can be the root of an editor tree
     */
    val isRoot: Boolean

    /**
     * This method should be considered an implementation detail.
     * It is likely to be removed soon and using it can cause all sorts of bugs.
     */
    @Deprecated("Treat as private")
    fun initParent(parent: Editor<*>)

    /**
     * This method should be considered an implementation detail.
     * It is likely to be removed soon and using it can cause all sorts of bugs.
     */
    @Deprecated("Treat as private")
    fun initExpander(expander: Expander<@UnsafeVariance R, *>)

    @Deprecated("Treat as private")
    fun initAccessor(acc: EditorAccessor)

    /**
     * Paste the given [editor] into this [Editor] if it is supported.
     * @return `true` only if pasting the given [editor] is supported.
     */
    fun paste(editor: Editor<*>): Boolean

    /**
     * Returns `true` only if this [Editor] supports copy/paste in principle.
     * The default implementation returns `false`.
     */
    fun supportsCopyPaste(): Boolean = false

    /**
     * Return the child denoted by the given [accessor] or throw a [InvalidAccessorException] if there is no such child
     */
    fun getSubEditor(accessor: EditorAccessor): Editor<*>

    @Deprecated("Treat as private")
    fun setFile(editor: FileEditor<*>)
}