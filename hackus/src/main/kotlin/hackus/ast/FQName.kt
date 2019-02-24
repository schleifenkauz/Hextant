/**
 *@author Nikolaus Knop
 */

package hackus.ast

import java.nio.file.Path
import java.nio.file.Paths

/**
 * A fully qualified Java name consisting of a package part and an identifier
 * * In hackus FQNames are used as left hand sides of definitions
 * @constructor
 * @param pkg the subsequent package names, may be empty
 * @param name the class name
 * * Sample "java.lang.String" == `FQName(listOf(JIdent.of("java"), JIdent.of("lang")), JIdent.of("String"))`
 * * Sample "TopLevelClass" == `FQName(emptyList(), JIdent.of("TopLevelClass"))`
 */
class FQName(val pkg: List<JIdent>, val name: JIdent) {
    /**
     * Return the fully qualified name in string form
     * * Sample: `FQName(listOf(JIdent.of("java"), JIdent.of("lang")), JIdent.of("String")).toString() == "java.lang.String"`
     */
    override fun toString(): String = buildString {
        for (n in pkg) {
            append(n)
            append('.')
        }
        append(name)
    }

    /**
     * @return `true` if and only if the [other] [FQName] represents exactly the same fully qualified name as this one
     */
    override fun equals(other: Any?): Boolean = when {
        this === other          -> true
        other !is FQName        -> false
        other.name != this.name -> false
        other.pkg != this.pkg   -> false
        else                    -> true
    }

    override fun hashCode(): Int = pkg.hashCode() * 33 + name.hashCode()

    /**
     * Return a relative path which would lead from the source root to the class with this fully qualified name
     *
     * Samples:
     * * "java.lang.String" -> "java/lang/String.java"
     * * "ClassInDefaultPackage" -> "ClassInDefaultPackage.java"
     * * "path.to.SomeClass" -> "path/to/SomeClass.java"
     */
    fun toPath(): Path =
        if (pkg.isEmpty()) Paths.get(name.toString())
        else {
            val first = pkg.first().toString()
            val more = pkg.drop(1) + name
            val asArr = Array(pkg.size) { idx -> more[idx].toString() }
            Paths.get(first, *asArr)
        }
}