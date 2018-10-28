/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.base

import org.nikok.hextant.*
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.impl.SelectionDistributor
import org.nikok.hextant.core.impl.myLogger
import org.nikok.reaktive.value.Variable
import org.nikok.reaktive.value.base.AbstractVariable
import org.nikok.reaktive.value.observe

/**
 * The base class of all [Editor]s
 * It manages selection and showing errors of the [Editable]s in the associated [EditorView]
 * @constructor
 * @param E the type of [Editable] edited by this [Editor]
 * @param editable the [Editable] edited by this [Editor]
 * @param view the view associated with this [Editor]
 */
abstract class AbstractEditor<E : Editable<*>>(
    final override val editable: E, final override val view: EditorView
) : Editor<E> {
    private val isOkObserver = editable.isOk.observe("Observe isOk") { isOk ->
        logger.info("$editable is ok = $isOk")
        view.error(isError = !isOk)
    }

    private val selectionDistributor = HextantPlatform[Internal, SelectionDistributor]

    override val isSelected: Boolean get() = isSelectedVar.get()

    private val isSelectedVar: Variable<Boolean> = object : AbstractVariable<Boolean>() {
        private var value = false

        override val description: String
            get() = "Is ${this@AbstractEditor} selected"

        override fun doSet(value: Boolean) {
            this.value = value
            logger.info("$this is selected = $value")
            view.select(value)
        }

        override fun get(): Boolean = value
    }

    override fun select() {
        logger.info("selecting $this")
        selectionDistributor.select(this, isSelectedVar)
    }

    override fun toggleSelection() {
        logger.info("toggling selection for $this")
        selectionDistributor.toggleSelection(this, isSelectedVar)
    }

    companion object {
        val logger by myLogger()
    }
}