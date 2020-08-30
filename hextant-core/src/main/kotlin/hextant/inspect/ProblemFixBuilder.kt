/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import hextant.command.Builder

/**
 * A Builder for [ProblemFix]s
 */
@Builder
class ProblemFixBuilder<T : Any> @PublishedApi internal constructor() {
    /**
     * The description of the built problem, must be specified
     */
    lateinit var description: String
    private lateinit var doFix: InspectionBody<T>.() -> Unit

    private var applicable: InspectionBody<T>.() -> Boolean = { true }

    /**
     * The built problem will fix problems by invoking [fix]
     */
    fun fixingBy(fix: InspectionBody<T>.() -> Unit) {
        doFix = fix
    }

    /**
     * The built problem will be applicable if [predicate] returns `true`
     */
    fun applicableIf(predicate: InspectionBody<T>.() -> Boolean) {
        applicable = predicate
    }

    @PublishedApi internal fun build(): ProblemFix<T> = ProblemFixImpl(description, doFix, applicable)
}