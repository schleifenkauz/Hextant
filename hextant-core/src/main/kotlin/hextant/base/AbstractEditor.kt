/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.Context
import hextant.Editor
import hextant.core.editor.Expander
import hextant.project.editor.FileEditor
import hextant.serial.EditorAccessor
import hextant.serial.InvalidAccessorException
import reaktive.collection.ReactiveCollection
import reaktive.list.reactiveList
import reaktive.value.*

/**
 * Skeletal implementation for [Editor]s
 */
@Suppress("OverridingDeprecatedMember")
abstract class AbstractEditor<out R : Any, in V : Any>(override val context: Context) : Editor<R>,
                                                                                        AbstractController<V>() {
    private val _parent = reactiveVariable<Editor<*>?>(null)

    final override val parent: ReactiveValue<Editor<*>?> get() = _parent

    private val _children = reactiveList<Editor<*>>()

    override val children: ReactiveCollection<Editor<*>> get() = _children

    private val _expander = reactiveVariable<Expander<R, *>?>(null)

    override val expander: ReactiveValue<Expander<*, *>?> get() = _expander

    override fun setParent(newParent: Editor<*>?) {
        _parent.set(newParent)
    }

    override fun setExpander(newExpander: Expander<@UnsafeVariance R, *>?) {
        _expander.set(newExpander)
    }

    final override var accessor: EditorAccessor? = null
        private set

    override fun setAccessor(acc: EditorAccessor) {
        accessor = acc
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        throw InvalidAccessorException(accessor)
    }

    /**
     * Makes the [editor] a child of this editor and just returns it
     */
    protected fun <E : Editor<*>> child(editor: E): E {
        @Suppress("DEPRECATION")
        editor.setParent(this)
        _children.now.add(editor)
        return editor
    }

    /**
     * Make all the given editors children of this editor
     */
    protected fun children(vararg children: Editor<*>) {
        for (c in children) child(c)
    }

    private var _file: FileEditor<*>? = null

    override fun setFile(editor: FileEditor<*>) {
        _file = editor
    }

    override val file: FileEditor<*>?
        get() = _file ?: parent.now?.file

    override val isRoot: Boolean
        get() = _file != null
}