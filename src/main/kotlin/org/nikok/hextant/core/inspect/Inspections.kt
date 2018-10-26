/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.inspect

import org.nikok.hextant.prop.CorePermissions.Internal
import org.nikok.hextant.prop.CorePermissions.Public
import org.nikok.hextant.prop.Property
import org.nikok.reaktive.value.ReactiveBoolean
import kotlin.reflect.KClass

class Inspections private constructor() {
    private val registrars: MutableMap<KClass<*>, InspectionRegistrar<Any>> = HashMap()

    fun <T : Any> of(cls: KClass<out T>): InspectionRegistrar<T> {
        val registrar = registrars.getOrPut(cls) {
            InspectionRegistrar()
        }
        @Suppress("UNCHECKED_CAST") return registrar as InspectionRegistrar<T>
    }

    fun getProblems(obj: Any): Set<Problem> = of(obj::class).getProblems(obj)

    fun hasError(obj: Any): ReactiveBoolean = of(obj::class).hasError(obj)

    fun hasWarning(obj: Any): ReactiveBoolean = of(obj::class).hasWarning(obj)

    companion object: Property<Inspections, Public, Internal>("inspections") {
        fun newInstance(): Inspections = Inspections()
    }
}