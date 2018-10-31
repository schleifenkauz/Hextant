/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.base

import org.nikok.hextant.*
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditorFactory
import org.nikok.hextant.core.impl.SelectionDistributor
import org.nikok.reaktive.value.Variable
import org.nikok.reaktive.value.base.AbstractVariable
import org.nikok.reaktive.value.observe
import java.lang.ref.WeakReference

/**
 * The base class of all [Editor]s
 * It manages selection and showing errors of the [Editable]s in the associated [EditorView]
 * @constructor
 * @param E the type of [Editable] edited by this [Editor]
 * @param V the type of [EditorView]'s that can be managed by this editor
 * @param editable the [Editable] edited by this [Editor]
 */
abstract class AbstractEditor<E : Editable<*>, V : EditorView>(
    final override val editable: E,
    private val editorFactory: EditorFactory = HextantPlatform[Public, EditorFactory]
) : Editor<E> {
    private val mutableViews = mutableSetOf<WeakReference<V>>()

    /**
     * @return a sequence of all views registered to this editor
    */
    protected val views: Sequence<V>
        get() {
            val itr = mutableViews.iterator()
            tailrec fun next(): V? =
                    if (!itr.hasNext()) null
                    else {
                        val nxt = itr.next()
                        if (nxt.get() != null) nxt.get()
                        else {
                            itr.remove()
                            next()
                        }
                    }
            return generateSequence(::next)
        }

    protected inline fun views(crossinline action: V.() -> Unit) {
        views.forEach { v -> v.onGuiThread { v.action() } }
    }

    /**
     * Add the specified [view] to this editor, such that it will be notified when the editor is modified
     * * eventually the editor will directly call methods of the view
     * so be careful when adding a view in the constructor
     * * Adding a view to an editor will not prevent the view from being garbage collected
    */
    fun addView(view: V) {
        mutableViews.add(WeakReference(view))
        viewAdded(view)
    }

    /**
     * Is called when the specified [view] is added
     * * The default implementation does nothing
    */
    protected open fun viewAdded(view: V) {}

    private val isOkObserver = editable.isOk.observe("Observe isOk") { isOk ->
        views.forEach { v -> v.error(isError = !isOk) }
    }

    private val selectionDistributor = HextantPlatform[Internal, SelectionDistributor]

    final override val isSelected: Boolean get() = isSelectedVar.get()

    private val isSelectedVar: Variable<Boolean> = object : AbstractVariable<Boolean>() {
        private var value = false

        override val description: String
            get() = "Is ${this@AbstractEditor} selected"

        override fun doSet(value: Boolean) {
            this.value = value
            views.forEach { it.select(isSelected = value) }
        }

        override fun get(): Boolean = value
    }

    final override fun select() {
        selectionDistributor.select(this, isSelectedVar)
    }

    final override fun toggleSelection() {
        selectionDistributor.toggleSelection(this, isSelectedVar)
    }

    override val parent: Editor<*>?
        get() = editable.parent?.let { p -> editorFactory.resolveEditor(p) }
    override val children: Collection<Editor<*>>?
        get() = editable.children?.map { child -> editorFactory.resolveEditor(child) }
}