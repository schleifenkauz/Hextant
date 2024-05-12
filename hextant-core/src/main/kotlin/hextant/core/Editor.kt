/**
 * @author Nikolaus Knop
 */

package hextant.core

import hextant.context.Context
import hextant.core.editor.Expander
import hextant.serial.*
import reaktive.collection.ReactiveCollection
import reaktive.value.ReactiveValue

/**
 * An editor for results of type [R]
 */
interface Editor<out R> : SnapshotAware {
    /**
     * A [reaktive.value.ReactiveValue] holding the result of compiling the content of the editor
     */
    val result: ReactiveValue<R>

    /**
     * The parent of this Editor or `null` if this Editor is the root
     */
    val parent: Editor<*>?

    /**
     * @return the location of this editor relative its parent
     */
    val accessor: ReactiveValue<EditorAccessor?>

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
    val file: VirtualFile<Editor<*>>?

    /**
     * Returns `true` only if this editor can be the root of an editor tree
     */
    val isRoot: Boolean

    /**
     * This method should be considered an implementation detail.
     * It is likely to be removed soon and using it can cause all sorts of bugs.
     */
    @Deprecated("Treat as internal")
    fun initParent(parent: Editor<*>)

    /**
     * Called when this editor is added to the editor tree.
     * @param parent the editor of this editor in the editor tree.
     */
    fun onInitParent(parent: Editor<*>) {}

    /**
     * This method should be considered an implementation detail.
     * It is likely to be removed soon and using it can cause all sorts of bugs.
     */
    @Deprecated("Treat as internal")
    fun initExpander(expander: Expander<*, *>)

    /**
     * This method should be considered an implementation detail.
     * It is likely to be removed soon and using it can cause all sorts of bugs.
     */
    @Deprecated("Treat as internal")
    fun setAccessor(acc: EditorAccessor)

    /**
     * This method should be considered an implementation detail.
     * It is likely to be removed soon and using it can cause all sorts of bugs.
     */
    @Deprecated("Treat as internal")
    fun setFile(file: VirtualFile<Editor<*>>)

    /**
     * Paste the given [snapshot] into this [Editor] if it is supported.
     * @return `true` only if pasting the given [snapshot] is supported.
     */
    fun paste(snapshot: Snapshot<out Editor<*>>): Boolean

    /**
     * Returns `true` only if this [Editor] supports copy/paste in principle.
     * The default implementation returns `false`.
     */
    fun supportsCopyPaste(): Boolean = false

    /**
     * Return the child denoted by the given [accessor] or throw a [InvalidAccessorException] if there is no such child
     */
    fun getSubEditor(accessor: EditorAccessor): Editor<*>
}