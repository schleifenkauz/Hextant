/**
 *@author Nikolaus Knop
 */

package hackus.ast

/**
 * A **valid** Java identifier with the only difference that dollar signs (`$`) are not allowed
 */
class JIdent private constructor(private val string: String) {
    /**
     * @return the string representing this identifier
     */
    override fun toString(): String = string

    /**
     * JIdents are equal if they have the same name
     */
    override fun equals(other: Any?): Boolean = when {
        other === this              -> true
        other !is JIdent            -> false
        other.string != this.string -> false
        else                        -> true
    }

    /**
     * @return the hashcode of the passed string
     */
    override fun hashCode(): Int = string.hashCode()

    companion object {
        /**
         * @return `true` only if the passed [name] is a valid java identifier and contains no dollar signs (`$`)
         */
        fun isValid(name: String): Boolean =
            name.isNotEmpty() &&
                    name.first().isJavaIdentifierStart() &&
                    name.all { it.isJavaIdentifierPart() && it != '$' }

        /**
         * @return a [JIdent] with the specified [string]
         * @throws IllegalArgumentException if the specified [string] is not valid, see [isValid]
         */
        fun of(string: String): JIdent {
            require(isValid(string))
            return JIdent(string)
        }

        /**
         * @return a [JIdent] with the specified [string] or `null` if the passed string is not valid ([isValid])
         */
        fun orNull(string: String): JIdent? = if (isValid(string)) JIdent(string) else null
    }
}