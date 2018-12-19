/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.inspect

import org.nikok.hextant.core.inspect.Severity.Error
import org.nikok.hextant.core.inspect.Severity.Warning
import org.nikok.reaktive.value.*

internal class InspectionManager<T>(private val inspected: T) {
    private val warningCount = reactiveVariable("Warning count of $inspected", 0)

    private val errorCount = reactiveVariable("Error count of $inspected", 0)

    private val inspections = mutableSetOf<Inspection<T>>()

    fun addInspection(inspectionFactory: (T) -> Inspection<T>) {
        val inspection = inspectionFactory.invoke(inspected)
        inspections.add(inspection)
        if (inspection.isProblem.now) {
            updateProblemCount(inspection.severity) { it + 1 }
        }
        inspection.isProblem.observe("Observe isProblem of inspection") { isProblem ->
            if (isProblem) {
                updateProblemCount(inspection.severity) { it - 1 }
            } else {
                updateProblemCount(inspection.severity) { it - 1 }
            }
        }
    }

    private fun updateProblemCount(severity: Severity, update: (Int) -> Int) = when (severity) {
        Error   -> errorCount.set(update(errorCount.now))
        Warning -> warningCount.set(update(warningCount.now))
    }

    val hasError = errorCount.map("$inspected has error") { it > 0 }
    val hasWarning = warningCount.map("$inspected has warning") { it > 0 }

    fun problems(): Set<Problem> =
            inspections.mapNotNullTo(mutableSetOf()) { if (it.isProblem.now) it.getProblem() else null }
}