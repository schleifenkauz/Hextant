/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import bundles.Property
import hextant.context.Internal
import kollektion.ClassDAG
import reaktive.value.ReactiveBoolean
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

/**
 * Objects of this class are used to register [Inspection]s for targets.
 */
class Inspections private constructor() {
    private val managers: MutableMap<KClass<*>, MutableMap<Any, InspectionManager<*>>> = mutableMapOf()
    private val inspections = mutableMapOf<KClass<*>, MutableSet<Inspection<*>>>()
    private val dag = ClassDAG()
    private val all = mutableSetOf<Inspection<*>>()

    private fun inspections(cls: KClass<*>) = inspections.getOrPut(cls) { mutableSetOf() }
    private fun managers(cls: KClass<*>) = managers.getOrPut(cls) { WeakHashMap() }

    private fun addInspection(cls: KClass<*>, inspection: Inspection<*>) {
        inspections(cls).add(inspection)
        for (manager in managers(cls).values) {
            @Suppress("UNCHECKED_CAST")
            manager.addInspection(inspection as Inspection<Any>)
        }
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
    fun <T : Any> register(cls: KClass<out T>, inspection: Inspection<T>) {
        all.add(inspection)
        visitClass(cls)
        for (c in dag.subclassesOf(cls)) {
            addInspection(c, inspection)
        }
    }

    /**
     * Return a set of all problems that the inspection for this object report.
     */
    fun <T : Any> getProblems(obj: T): Set<Problem<Any>> = getManagerFor(obj).problems()

    internal fun <T : Any> getManagerFor(obj: T): InspectionManager<Any> {
        val cls = obj::class
        visitClass(cls)
        val m = managers(cls).getOrPut(obj) {
            InspectionManager(obj, this).apply {
                for (i in inspections(cls)) {
                    @Suppress("UNCHECKED_CAST")
                    (addInspection(i as Inspection<T>))
                }
            }
        }
        @Suppress("UNCHECKED_CAST")
        return m as InspectionManager<Any>
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
    fun all(): Collection<Inspection<*>> = all

    fun <T : Any> disable(inspection: Inspection<T>) {
        TODO("not implemented")
    }

    companion object : Property<Inspections, Any, Internal>("inspections") {
        /**
         * Create a new [Inspections] object.
         */
        fun newInstance(): Inspections = Inspections()
    }
}