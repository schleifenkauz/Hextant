/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.Context
import hextant.Editor
import hextant.core.editor.Expander
import hextant.core.editor.getTypeArgument
import hextant.project.editor.FileEditor
import hextant.serial.EditorAccessor
import hextant.serial.InvalidAccessorException
import kserial.*
import reaktive.collection.ReactiveCollection
import reaktive.list.reactiveList

/**
 * Skeletal implementation for [Editor]s
 */
@Suppress("OverridingDeprecatedMember")
abstract class AbstractEditor<out R : Any, in V : Any>(
    override val context: Context
) : Editor<R>, AbstractController<V>(), Serializable {
    final override var parent: Editor<*>? = null
        private set

    private val _children = reactiveList<Editor<*>>()

    override val children: ReactiveCollection<Editor<*>> get() = _children

    final override var expander: Expander<*, *>? = null
        private set

    override fun initParent(parent: Editor<*>) {
        this.parent = parent
    }

    override fun initExpander(expander: Expander<@UnsafeVariance R, *>) {
        this.expander = expander
    }

    final override var accessor: EditorAccessor? = null
        private set

    override fun initAccessor(acc: EditorAccessor) {
        accessor = acc
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        throw InvalidAccessorException(accessor)
    }

    /**
     * Makes the [editor] a child of this editor and just returns it
     */
    protected fun <E : Editor<*>> addChild(editor: E): E {
        @Suppress("DEPRECATION")
        editor.initParent(this)
        _children.now.add(editor)
        return editor
    }

    /**
     * Make all the given editors children of this editor
     */
    protected fun children(vararg children: Editor<*>) {
        for (c in children) addChild(c)
    }

    private var _file: FileEditor<*>? = null

    override fun setFile(editor: FileEditor<*>) {
        _file = editor
    }

    override val file: FileEditor<*>?
        get() = _file ?: parent?.file

    override val isRoot: Boolean
        get() = _file != null

    override fun serialize(output: Output, context: SerialContext) {
        output.writeObject(createSnapshot())
    }

    override fun deserialize(input: Input, context: SerialContext) {
        val snapshot = input.readTyped<EditorSnapshot<AbstractEditor<*, *>>>()
        snapshot.reconstruct(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun paste(snapshot: EditorSnapshot<*>): Boolean {
        val cls = snapshot::class.getTypeArgument(EditorSnapshot::class, 0)
        if (cls != this::class) return false
        snapshot as EditorSnapshot<Editor<*>>
        snapshot.reconstruct(this)
        return true
    }
}