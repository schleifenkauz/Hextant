/**
 *@author Nikolaus Knop
 */

package hextant.inspect

/**
 * A problem represents a warning or an error which reminds the programmer that some code he has written be faulty.
 * @property source The element from which this problem is originating.
 * @property message The message which should be shown to the programmer
 * @property severity The severity of this problem
 * @property fixes The possible [ProblemFix]es to fix this [Problem]
 */
class Problem<T : Any>(
    src: T,
    val message: String,
    val severity: Severity,
    val fixes: Collection<ProblemFix<T>>
) {
    internal val source = InspectionBody.soft(src)

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
         * Whether the problem prevents the program from being executable.
         */
        abstract val isSevere: Boolean
    }
}