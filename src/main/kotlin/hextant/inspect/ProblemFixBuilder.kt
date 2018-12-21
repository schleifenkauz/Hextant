/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import hextant.command.Builder

/**
 * A Builder for [ProblemFix]s
*/
@Builder
class ProblemFixBuilder @PublishedApi internal constructor() {
    /**
     * The description of the built problem, must be specified
    */
    lateinit var description: String
    private lateinit var doFix: () -> Unit

    private var applicable: () -> Boolean = { true }

    /**
     * The built problem will fix problems by invoking [fix]
    */
    fun fixingBy(fix: () -> Unit) {
        doFix = fix
    }

    /**
     * The built problem will be applicable if [predicate] returns `true`
    */
    fun applicableIf(predicate: () -> Boolean) {
        applicable = predicate
    }

    @PublishedApi internal fun build(): ProblemFix = problemFix(description, doFix, applicable)
}