/**
 * @author Nikolaus Knop
 */

package hextant.inspect

import reaktive.value.ReactiveBoolean

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
 * @return a [Inspection] with the specified [description]
 * reporting a problem is [isProblem] is `true` and reporting a problem with [problem]
*/
inline fun inspection(
    description: String,
    isProblem: ReactiveBoolean,
    severity: Severity,
    location: Any,
    crossinline problem: () -> Problem?
): Inspection = object : Inspection {
    override val severity: Severity = severity

    override val isProblem: ReactiveBoolean = isProblem

    override val description = description

    override val location: Any = location

    override fun getProblem() = problem()
}

/**
 * @return an [Inspection] built with [block] inspecting the specified [inspected] value
*/
inline fun <reified T : Any> inspection(inspected: T, block: InspectionBuilder.() -> Unit): Inspection =
    InspectionBuilder().apply { location(inspected); block() }.build()


inline fun <reified T : Any> Inspections.of(): InspectionRegistrar<T> = of(T::class)