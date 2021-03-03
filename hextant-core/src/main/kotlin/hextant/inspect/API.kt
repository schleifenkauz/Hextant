/**
 * @author Nikolaus Knop
 */

package hextant.inspect


/**
 * @return a [ProblemFix] built with [block]
 */
inline fun <T : Any> problemFix(block: ProblemFixBuilder<T>.() -> Unit) = ProblemFixBuilder<T>().apply(block).build()

/**
 * @return an [Inspection] built with [block] inspecting objects1 of type [T].
 */
inline fun <reified T : Any> inspection(block: InspectionBuilder<T>.() -> Unit): Inspection<T> =
    InspectionBuilder(T::class).apply(block).build()

/**
 * Build an inspection with the given [builder] and register it.
 */
inline fun <reified T : Any> Inspections.registerInspection(builder: InspectionBuilder<T>.() -> Unit) {
    register(inspection(builder))
}