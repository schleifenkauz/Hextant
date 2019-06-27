/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import reaktive.event.*
import reaktive.value.ReactiveBoolean
import java.util.*

/**
 * Used to register [Inspection]s for objects of type [T]
 */
class InspectionRegistrar<T : Any> internal constructor(private val inspections: Inspections) {
    private val managers: MutableMap<T, InspectionManager<T>> = WeakHashMap()

    private val inspectionFactories: MutableList<(T) -> Inspection> = mutableListOf()

    private val addInspection = event<(T) -> Inspection>()

    private val addedInspection = addInspection.stream

    private val passdownSubscriptions = mutableListOf<Subscription>()

    internal fun getManagerFor(obj: T): InspectionManager<T> = getInspectionManager(obj)

    @Deprecated("Treat as private")
    internal fun <C : T> passdownInspectionsTo(child: InspectionRegistrar<C>) {
        inspectionFactories.forEach { child.register(it) }
        val subscription = addedInspection.subscribe { factory -> child.register(factory) }
        passdownSubscriptions.add(subscription)
    }

    /**
     * Register the specified [inspection]
     */
    fun register(inspection: (T) -> Inspection) {
        inspectionFactories.add(inspection)
        for (m in managers.values) {
            m.addInspection(inspection)
        }
        addInspection.fire(inspection)
    }

    /**
     * Build an inspection with the given builder lambda and register it
     */
    fun registerInspection(builder: InspectionBuilder<T>.() -> Unit) {
        val inspection = { inspected: T ->
            InspectionBuilder(inspected).apply {
                location(inspected)
                builder()
            }.build()
        }
        register(inspection)
    }

    private fun getInspectionManager(inspected: T): InspectionManager<T> {
        return managers.getOrPut(inspected) { createInspectionManager(inspected) }
    }

    private fun createInspectionManager(inspected: T): InspectionManager<T> {
        val manager = InspectionManager(inspected, inspections)
        for (iFact in inspectionFactories) {
            manager.addInspection(iFact)
        }
        return manager
    }


    internal fun getProblems(inspected: T): Set<Problem> {
        val m = getInspectionManager(inspected)
        return m.problems()
    }

    internal fun hasError(inspected: T): ReactiveBoolean {
        val m = getInspectionManager(inspected)
        return m.hasError
    }

    internal fun hasWarning(inspected: T): ReactiveBoolean {
        val m = getInspectionManager(inspected)
        return m.hasWarning
    }
}