package hextant.lisp.ctx

import bundles.PublicProperty
import bundles.property

/**
 * The context in which an S-Expression is edited
*/
sealed class EditingContext {
    abstract val canReduceNormalized: Boolean

    /**
     * Context in which the expression can be saved and is part of the project.
    */
    object File: EditingContext() {
        override val canReduceNormalized: Boolean
            get() = true
    }

    /**
     * Context in which the expression can be **r**educed, **e**dited and **p**rinted.
     */
    object REPL : EditingContext() {
        override val canReduceNormalized: Boolean
            get() = false
    }

    companion object : PublicProperty<EditingContext> by property("editing context")
}