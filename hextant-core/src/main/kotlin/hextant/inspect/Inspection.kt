/**
 * @author Nikolaus Knop
 */

package hextant.inspect

import hextant.config.Feature
import hextant.config.FeatureType
import hextant.context.Context
import reaktive.value.ReactiveBoolean
import kotlin.reflect.KClass

/**
 * Interface for Inspections
 */
interface Inspection<in T : Any> : Feature {
    /**
     * The class of targets that are inspected by this inspection.
    */
    val targetClass: KClass<in T>

    /**
     * @return the description of this [Inspection]
     */
    override val description: String

    /**
     * Check whether
    */
    fun InspectionBody<T>.applies(): Boolean

    /**
     * @return a [ReactiveBoolean] holding `true` if this inspection reports a problem on the inspected object.
     */
    fun InspectionBody<T>.isProblem(): ReactiveBoolean

    /**
     * @return the severity of this problem
     */
    val severity: Problem.Severity

    /**
     * If a problem is reported this problem is registered for the given returned target.
     */
    fun InspectionBody<T>.location(): Any

    /**
     * @return the problem reported by this inspection on the given target.
     * If this inspection does not report on the given target the behaviour is implementation-specific.
     */
    fun InspectionBody<T>.getProblem(): Problem<in T>

    companion object: FeatureType<Inspection<*>>("Inspection") {
        override fun onEnable(feature: Inspection<*>, context: Context) {
            super.onEnable(feature, context)
            context[Inspections].register(feature)
        }

        override fun onDisable(feature: Inspection<*>, context: Context) {
            super.onDisable(feature, context)
            context[Inspections].unregister(feature)
        }
    }
}