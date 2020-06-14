/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import hextant.inspect.Severity.Error
import hextant.inspect.Severity.Warning
import org.nikok.kref.forcedWeak
import reaktive.Observer
import reaktive.value.binding.and
import reaktive.value.binding.notEqualTo
import reaktive.value.now
import reaktive.value.reactiveVariable
import java.util.*

internal class InspectionManager<T : Any>(inspected: T, private val inspections: Inspections) : InspectionBody<T> {
    override val inspected by forcedWeak(inspected)

    private val warningCount = reactiveVariable(0)

    private val errorCount = reactiveVariable(0)

    private val reportingInspections = mutableSetOf<Inspection<T>>()

    private fun reportingInspections(): MutableSet<in Inspection<T>> = reportingInspections

    private val observers = LinkedList<Observer>()

    fun addInspection(inspection: Inspection<T>) = inspection.run {
        @Suppress("UNCHECKED_CAST")
        inspection as Inspection<Any>
        val actualTarget =
            if (inspected === location()) this@InspectionManager
            else inspections.getManagerFor(location())
        val problem = inspection.isEnabled and isProblem()
        if (problem.now) {
            actualTarget.reportingInspections().add(inspection)
            actualTarget.changeCount(inspection.severity, +1)
        }
        val obs = problem.observe { _, _, isProblem ->
            if (isProblem) {
                actualTarget.reportingInspections().add(inspection)
                actualTarget.changeCount(inspection.severity, +1)
            } else {
                actualTarget.reportingInspections().remove(inspection)
                actualTarget.changeCount(inspection.severity, -1)
            }
        }
        observers.add(obs)
    }

    private fun changeCount(type: Severity, delta: Int) {
        if (type == Error) errorCount.now += delta
        else if (type == Warning) warningCount.now += delta
    }

    val hasError = errorCount.notEqualTo(0)
    val hasWarning = warningCount.notEqualTo(0)

    fun problems(): Set<Problem<T>> = reportingInspections.mapTo(mutableSetOf()) { it.run { getProblem() } }
}