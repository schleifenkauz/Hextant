/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.inspect

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.bundle.Property
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
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
        fun newInstance(platform: HextantPlatform): Inspections = Inspections()
    }
}