package hextant.core.editor

interface ChoiceSource<C : Any> {
    /**
     * Select the given [choice]
     */
    fun select(choice: C)

    fun toString(choice: C): String

    fun fromString(str: String): C?

    fun choices(): List<C>
}