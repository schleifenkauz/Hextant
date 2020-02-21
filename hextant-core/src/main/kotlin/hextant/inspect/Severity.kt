/**
 * @author Nikolaus Knop
 */

package hextant.inspect

/**
 * The severity of a problem.
 */
enum class Severity {
    /**
     * Indicates a warning
     */
    Warning {
        override val isSevere: Boolean
            get() = false

        override fun toString(): String = "warning"
    },
    /**
     * Indicates an error
     */
    Error {
        override val isSevere: Boolean
            get() = true

        override fun toString(): String = "error"
    };

    /**
     * @return whether the problem prevents the program from being runnable
     */
    abstract val isSevere: Boolean

    companion object {
        /**
         * @return [Error] if [severe] is `true` else [Warning]
         */
        fun of(severe: Boolean) = if (severe) Error else Warning
    }
}