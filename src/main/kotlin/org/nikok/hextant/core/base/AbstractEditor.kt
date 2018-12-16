/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.base

import org.nikok.hextant.*
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.EditorFactory
import org.nikok.hextant.core.impl.SelectionDistributor
import org.nikok.reaktive.value.Variable
import org.nikok.reaktive.value.base.AbstractVariable
import org.nikok.reaktive.value.observe

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
    platform: HextantPlatform
) : Editor<E>, AbstractController<V>() {

    private val isOkObserver = editable.isOk.observe("Observe isOk") { isOk ->
        views.forEach { v -> v.error(isError = !isOk) }
    }

    override fun onGuiThread(view: V, action: V.() -> Unit) {
        view.onGuiThread { view.action() }
    }

    private val selectionDistributor = platform[Internal, SelectionDistributor]
    private val editorFactory = platform[EditorFactory]

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
        get() = TODO()
}