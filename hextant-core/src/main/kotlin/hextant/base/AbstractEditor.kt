/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.Context
import hextant.Editor
import hextant.core.editor.Expander
import reaktive.collection.ReactiveCollection
import reaktive.list.reactiveList
import reaktive.value.ReactiveValue
import reaktive.value.reactiveVariable

/**
 * Skeletal implementation for [Editor]s
 */
abstract class AbstractEditor<out R : Any, in V : Any>(val context: Context) : Editor<R>, AbstractController<V>() {
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

    /**
     * Makes the [editor] a child of this editor and just returns it
     */
    protected fun <E : Editor<*>> child(editor: E): E {
        editor.setParent(this)
        _children.now.add(editor)
        return editor
    }
}