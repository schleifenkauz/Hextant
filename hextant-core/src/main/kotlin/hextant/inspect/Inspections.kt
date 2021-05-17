/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.inspect

import bundles.Property
import bundles.property
import hextant.context.Internal
import kollektion.ClassDAG
import kollektion.Counter
import kollektion.MultiMap
import reaktive.value.ReactiveBoolean
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

/**
 * Objects of this class are used to register [Inspection]s for targets.
 */
class Inspections private constructor() {
    private val inspections = mutableMapOf<KClass<*>, MutableSet<Inspection<*>>>()
    private val dag = ClassDAG()
    private val instances = MultiMap<KClass<*>, Any>()
    private val managers: MutableMap<Any, InspectionManager> = WeakHashMap()
    private val all = Counter<Inspection<*>>()

    private fun inspections(cls: KClass<*>) = inspections.getOrPut(cls) { mutableSetOf() }

    private fun addInspection(cls: KClass<*>, inspection: Inspection<*>) {
        inspection as Inspection<Any>
        inspections(cls).add(inspection)
        for (obj in instances[cls]) {
            addInspection(inspection, obj)
        }
    }

    private fun addInspection(inspection: Inspection<Any>, obj: Any) {
        val applies = inspection.run { InspectionBody.strong(obj).applies() }
        if (!applies) return
        val manager = getManagerForLocation(inspection, obj)
        val applied = AppliedInspection(obj, inspection)
        manager.addInspection(applied)
    }

    private fun removeInspection(cls: KClass<*>, inspection: Inspection<*>) {
        inspection as Inspection<Any>
        inspections(cls).remove(inspection)
        for (obj in instances[cls]) {
            if (removeInspection(inspection, obj)) return
        }
    }

    private fun removeInspection(inspection: Inspection<Any>, obj: Any): Boolean {
        val applies = inspection.run { InspectionBody.strong(obj).applies() }
        if (!applies) return true
        val manager = getManagerForLocation(inspection, obj)
        val applied = AppliedInspection(obj, inspection)
        manager.removeInspection(applied)
        return false
    }

    private fun getManagerForLocation(inspection: Inspection<Any>, obj: Any): InspectionManager {
        val loc = inspection.run { InspectionBody.strong(obj).location() }
        return getManagerFor(loc)
    }

    private fun visitClass(cls: KClass<*>) {
        if (dag.insert(cls)) {
            for (c in cls.superclasses) {
                visitClass(c)
                for (inspection in inspections(c)) {
                    addInspection(cls, inspection)
                }
            }
        }
    }

    /**
     * Register the given [inspection] for instances of the given class.
     */
    fun <T : Any> register(inspection: Inspection<T>) {
        all.add(inspection)
        visitClass(inspection.targetClass)
        for (c in dag.subclassesOf(inspection.targetClass)) {
            addInspection(c, inspection)
        }
    }

    /**
     * Registers a previously registered [inspection].
     * @throws IllegalStateException if the given [inspection] was not registered previously
     */
    fun <T : Any> unregister(inspection: Inspection<T>) {
        check(all.remove(inspection)) { "Cannot unregister inspection $inspection because it was not registered before" }
        for (c in dag.subclassesOf(inspection.targetClass)) {
            removeInspection(c, inspection)
        }
    }

    /**
     * Return a set of all problems that the inspection for this object report.
     */
    fun <T : Any> getProblems(obj: T): Set<Problem<*>> = getManagerFor(obj).problems()

    private fun getManagerFor(obj: Any): InspectionManager {
        visitClass(obj::class)
        if (instances[obj::class].add(obj)) {
            managers[obj] = InspectionManager()
            for (inspection in inspections(obj::class)) {
                addInspection(inspection as Inspection<Any>, obj)
            }
        }
        return managers[obj]!!
    }

    /**
     * Return a [ReactiveBoolean] that holds `true` only if any inspections report an error on the given object.
     */
    fun hasError(obj: Any): ReactiveBoolean = getManagerFor(obj).hasError

    /**
     * Return a [ReactiveBoolean] that holds `true` only if any inspections report a warning on the given object.
     */
    fun hasWarning(obj: Any): ReactiveBoolean = getManagerFor(obj).hasWarning

    /**
     * Return a collection containing all registered completions.
     */
    fun all(): Collection<Inspection<*>> = all.asSet()

    companion object : Property<Inspections, Internal> by property("inspections") {
        /**
         * Create a new [Inspections] object.
         */
        fun newInstance(): Inspections = Inspections()
    }
}