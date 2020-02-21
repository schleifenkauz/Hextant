/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import hextant.bundle.Internal

import hextant.bundle.Property
import reaktive.value.ReactiveBoolean
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

/**
 * Objects of this class are used to register [Inspection]s for targets.
 */
class Inspections private constructor() {
    private val registrars: MutableMap<KClass<*>, InspectionRegistrar<Any>> = HashMap()

    /**
     * Return the [InspectionRegistrar] for the given class
     */
    fun <T : Any> of(cls: KClass<out T>): InspectionRegistrar<T> {
        val registrar = registrars.getOrPut(cls) {
            InspectionRegistrar<Any>(this).also { reg ->
                cls.superclasses.forEach {
                    @Suppress("DEPRECATION") //Only used here
                    of(it).passdownInspectionsTo(reg)
                }
            }
        }
        @Suppress("UNCHECKED_CAST") return registrar as InspectionRegistrar<T>
    }

    /**
     * Return a set of all problems that the inspection for this object report.
     */
    fun getProblems(obj: Any): Set<Problem> = of(obj::class).getProblems(obj)

    /**
     * Return a [ReactiveBoolean] that holds `true` only if any inspections report an error on the given object.
     */
    fun hasError(obj: Any): ReactiveBoolean = of(obj::class).hasError(obj)

    /**
     * Return a [ReactiveBoolean] that holds `true` only if any inspections report a warning on the given object.
     */
    fun hasWarning(obj: Any): ReactiveBoolean = of(obj::class).hasWarning(obj)

    internal fun getManagerFor(obj: Any) = of(obj::class).getManagerFor(obj)

    companion object : Property<Inspections, Any, Internal>("inspections") {
        /**
         * Create a new [Inspections] object.
         */
        fun newInstance(): Inspections = Inspections()
    }
}