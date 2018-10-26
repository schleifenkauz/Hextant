/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.inspect

/**
 * A severity of a problem
 * Either error or warning
*/
enum class Severity {
    Warning {
        override val isSevere: Boolean
            get() = false

        override fun toString(): String = "warning"
    },
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