/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.inspect

import org.nikok.reaktive.value.ReactiveBoolean

/**
 * @return a [Problem] built with [block]
*/
inline fun problem(block: ProblemBuilder.() -> Unit): Problem = ProblemBuilder().apply(block).build()

/**
 * @return a [ProblemFix] with the specified [description] which is applicable if [applicable] returns `true` and fixes
 * problems by invoking [doFix]
*/
fun problemFix(description: String, doFix: () -> Unit, applicable: () -> Boolean = { true }): ProblemFix =
        ProblemFixImpl(description, doFix, applicable)

/**
 * @return a [ProblemFix] built with [block]
*/
inline fun problemFix(block: (ProblemFixBuilder).() -> Unit) = ProblemFixBuilder().apply(block).build()

/**
 * @return a [Inspection] inspecting the [inspected] value with the specified [description]
 * reporting a problem is [isProblem] is `true` and reporting a problem with [problem]
*/
inline fun <reified T : Any> inspection(
    inspected: T,
    description: String,
    isProblem: ReactiveBoolean,
    severity: Severity,
    crossinline problem: () -> Problem?
): Inspection<T> = object : Inspection<T> {
    override val severity: Severity = severity

    override val inspected = inspected

    override val isProblem: ReactiveBoolean = isProblem

    override val description = description

    override fun getProblem() = problem()
}

/**
 * @return an [Inspection] built with [block] inspecting the specified [inspected] value
*/
inline fun <reified T : Any> inspection(inspected: T, block: InspectionBuilder<T>.() -> Unit): Inspection<T> =
        InspectionBuilder(inspected).apply(block).build()


inline fun <reified T : Any> Inspections.of(): InspectionRegistrar<T> = of(T::class)