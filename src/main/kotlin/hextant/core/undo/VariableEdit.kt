/**
 *@author Nikolaus Knop
 */

package hextant.core.undo

import org.nikok.reaktive.value.Variable

class VariableEdit<T>(
    private val variable: Variable<T>,
    private val oldValue: T,
    private val newValue: T,
    override val actionDescription: String
) : AbstractEdit() {
    constructor(variable: Variable<T>, oldValue: T, actionDescription: String) : this(
        variable,
        oldValue,
        variable.get(),
        actionDescription
    )

    override fun doRedo() {
        variable.set(newValue)
    }

    override fun doUndo() {
        variable.set(oldValue)
    }

    @Suppress("UNCHECKED_CAST")
    override fun mergeWith(other: Edit): Edit? =
        if (other !is VariableEdit<*> || other.variable !== this.variable) null
        else VariableEdit(variable, this.oldValue, other.newValue as T, other.actionDescription)
}