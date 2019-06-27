/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import hextant.inspect.Severity.Error
import hextant.inspect.Severity.Warning
import reaktive.Observer
import reaktive.value.binding.notEqualTo
import reaktive.value.now
import reaktive.value.reactiveVariable
import java.util.*

internal class InspectionManager<T : Any>(private val inspected: T, private val inspections: Inspections) {
    private val warningCount = reactiveVariable(0)

    private val errorCount = reactiveVariable(0)

    private val reportingInspections = mutableSetOf<Inspection>()

    private val observers = LinkedList<Observer>()

    fun addInspection(inspectionFactory: (T) -> Inspection) {
        val inspection = inspectionFactory.invoke(inspected)
        val actualTarget =
            if (inspected === inspection.location) this
            else inspections.getManagerFor(inspection.location)
        if (inspection.isProblem.now) {
            actualTarget.reportingInspections.add(inspection)
            actualTarget.changeCount(inspection.severity, +1)
        }
        val obs = inspection.isProblem.observe { _, _, problem ->
            if (problem) {
                actualTarget.reportingInspections.add(inspection)
                actualTarget.changeCount(inspection.severity, +1)
            } else {
                actualTarget.reportingInspections.remove(inspection)
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

    fun problems(): Set<Problem> = reportingInspections.mapTo(mutableSetOf<Problem>()) {
        it.getProblem() ?: error("Inspection $it reported reported but returned null on getProblem()")
    }
}