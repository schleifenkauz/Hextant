/**
 * @author Nikolaus Knop
 */

package hextant.inspect

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
 * @return an [Inspection] built with [block] inspecting the specified [inspected] value
 */
inline fun <reified T : Any> inspection(inspected: T, block: InspectionBuilder<T>.() -> Unit): Inspection =
    InspectionBuilder(inspected).apply(block).build()


/**
 * @return the [InspectionRegistrar] for the class of [T]
 */
inline fun <reified T : Any> Inspections.of(): InspectionRegistrar<T> = of(T::class)