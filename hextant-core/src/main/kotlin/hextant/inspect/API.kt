/**
 * @author Nikolaus Knop
 */

package hextant.inspect

/**
 * @return a [Problem] built with [block]
 */
inline fun problem(block: ProblemBuilder<Any>.() -> Unit): Problem<Any> = ProblemBuilder<Any>().apply(block).build()

/**
 * @return a [ProblemFix] with the specified [description] which is applicable if [applicable] returns `true` and fixes
 * problems by invoking [doFix]
 */
fun <T : Any> problemFix(
    description: String,
    doFix: InspectionBody<T>.() -> Unit,
    applicable: InspectionBody<T>.() -> Boolean = { true }
): ProblemFix<T> = ProblemFixImpl(description, doFix, applicable)

/**
 * @return a [ProblemFix] built with [block]
 */
inline fun <T : Any> problemFix(block: ProblemFixBuilder<T>.() -> Unit) = ProblemFixBuilder<T>().apply(block).build()

/**
 * @return an [Inspection] built with [block] inspecting objects1 of type [T].
 */
inline fun <reified T : Any> inspection(block: InspectionBuilder<T>.() -> Unit): Inspection<T> =
    InspectionBuilder<T>().apply(block).build()

/**
 * Syntactic sugar for register(T::class, inspection)
 */
inline fun <reified T : Any> Inspections.register(inspection: Inspection<T>) {
    register(T::class, inspection)
}

/**
 * Build an inspection with the given [builder] and register it.
 */
inline fun <reified T : Any> Inspections.registerInspection(builder: InspectionBuilder<T>.() -> Unit) {
    register(inspection(builder))
}