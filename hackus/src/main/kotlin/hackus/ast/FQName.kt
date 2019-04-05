/**
 *@author Nikolaus Knop
 */

package hackus.ast

import hextant.*
import hextant.core.editable.TokenType
import java.nio.file.Path
import java.nio.file.Paths

/**
 * A fully qualified Java name consisting compile a package part and an identifier
 * * In hackus FQNames are used as left hand sides compile definitions
 */
class FQName private constructor(val fullName: String) {
    /**
     * Return the fully qualified name in string form
     */
    override fun toString(): String = fullName

    /**
     * @return `true` if and only if the [other] [FQName] represents exactly the same fully qualified name as this one
     */
    override fun equals(other: Any?): Boolean = when {
        this === other                  -> true
        other !is FQName                -> false
        other.fullName != this.fullName -> false
        else                            -> true
    }

    override fun hashCode(): Int = fullName.hashCode()

    /**
     * Return a relative path which would lead from the source root to the class with this fully qualified name
     *
     * Samples:
     * * "java.lang.String" -> "java/lang/String.java"
     * * "ClassInDefaultPackage" -> "ClassInDefaultPackage.java"
     * * "path.to.SomeClass" -> "path/to/SomeClass.java"
     */
    fun toPath(): Path {
        val subNames = fullName.split('.')
        return Paths.get(subNames.first(), *subNames.drop(1).toTypedArray())
    }

    companion object : TokenType<FQName> {
        private val identifier = Regex("[_a-zA-Z][_\$a-zA-Z0-9]*")

        override fun compile(tok: String): CompileResult<FQName> =
            tok.takeIf { it.split('.').all { part -> part.matches(identifier) } }
                .okOrErr { "Invalid fully qualified name $tok" }
                .map { FQName(it) }
    }
}