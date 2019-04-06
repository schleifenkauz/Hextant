/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import hextant.inspect.Severity.Error
import hextant.inspect.Severity.Warning
import reaktive.Observer
import reaktive.value.*
import reaktive.value.binding.notEqualTo
import java.util.*

internal class InspectionManager<T : Any>(private val inspected: T) {
    private val warningCount = reactiveVariable(0)

    private val errorCount = reactiveVariable(0)

    private val inspections = mutableSetOf<Inspection>()

    private val observers = LinkedList<Observer>()

    fun addInspection(inspectionFactory: (T) -> Inspection) {
        val inspection = inspectionFactory.invoke(inspected)
        inspections.add(inspection)
        if (inspection.isProblem.now) {
            updateProblemCount(inspection.severity) { it + 1 }
        }
        val obs = inspection.isProblem.observe { _, isProblem ->
            if (isProblem) {
                updateProblemCount(inspection.severity) { it + 1 }
            } else {
                updateProblemCount(inspection.severity) { it - 1 }
            }
        }
        observers.add(obs)
    }

    private fun updateProblemCount(severity: Severity, update: (Int) -> Int) = when (severity) {
        Error   -> errorCount.set(update(errorCount.now))
        Warning -> warningCount.set(update(warningCount.now))
    }

    val hasError = errorCount.notEqualTo(0)
    val hasWarning = warningCount.notEqualTo(0)

    fun problems(): Set<Problem> =
            inspections.mapNotNullTo(mutableSetOf()) { if (it.isProblem.now) it.getProblem() else null }
}