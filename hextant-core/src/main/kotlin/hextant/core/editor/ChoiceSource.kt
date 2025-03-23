package hextant.core.editor

import reaktive.value.ReactiveString

interface ChoiceSource<C: Any> {
    /**
     * Select the given [choice]
     */
    fun select(choice: C)

    fun toString(choice: C): ReactiveString

    fun choices(): List<C>
}