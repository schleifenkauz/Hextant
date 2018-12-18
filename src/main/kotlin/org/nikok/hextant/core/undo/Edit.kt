/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.undo

interface Edit {
    /**
     * @return `true` only if this [Edit] can be redone
     */
    val canRedo: Boolean

    /**
     * @return `true` only if this [Edit] can be undone
     */
    val canUndo: Boolean

    /**
     * Redo this edit
     * @throws IllegalStateException if this edit can not be redone
     */
    fun redo()

    /**
     * Undo this edit
     * @throws IllegalStateException if this edit can not be undone
     */
    fun undo()

    /**
     * * If this [Edit] cannot be merged with the [other] simply return `null`
     * * If it can be merged with the [other] [Edit] return the merged [Edit]
     */
    fun mergeWith(other: Edit): Edit?
}