/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import reaktive.value.ReactiveBoolean
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

class Inspections private constructor() {
    private val registrars: MutableMap<KClass<*>, InspectionRegistrar<Any>> = HashMap()

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

    fun getProblems(obj: Any): Set<Problem> = of(obj::class).getProblems(obj)

    fun hasError(obj: Any): ReactiveBoolean = of(obj::class).hasError(obj)

    fun hasWarning(obj: Any): ReactiveBoolean = of(obj::class).hasWarning(obj)

    internal fun getManagerFor(obj: Any) = of(obj::class).getManagerFor(obj)

    companion object : Property<Inspections, Public, Internal>("inspections") {
        fun newInstance(): Inspections = Inspections()
    }
}