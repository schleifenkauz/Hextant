/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.undo

import kotlin.reflect.KMutableProperty0

class PropertyEdit<T>(
    private val property: KMutableProperty0<T>,
    private val oldValue: T,
    private val newValue: T,
    override val actionDescription: String
) : AbstractEdit() {
    constructor(property: KMutableProperty0<T>, oldValue: T, actionDescription: String) : this(
        property,
        oldValue,
        property.get(),
        actionDescription
    )

    override fun doRedo() {
        property.set(newValue)
    }

    override fun doUndo() {
        property.set(oldValue)
    }

    @Suppress("UNCHECKED_CAST")
    override fun mergeWith(other: Edit): Edit? =
        if (other !is PropertyEdit<*> || other.property != this.property) null
        else PropertyEdit(property, this.oldValue, other.newValue as T, other.actionDescription)
}