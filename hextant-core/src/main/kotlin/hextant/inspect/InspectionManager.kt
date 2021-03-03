/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import hextant.inspect.Problem.Severity
import reaktive.Observer
import reaktive.value.*
import reaktive.value.binding.and
import reaktive.value.binding.notEqualTo

internal class InspectionManager {
    private val warningCount = reactiveVariable(0)
    private val errorCount = reactiveVariable(0)
    private val reportingInspections = mutableSetOf<AppliedInspection<*>>()
    private val observers = mutableMapOf<AppliedInspection<*>, Observer>()
    private val isProblem = mutableMapOf<AppliedInspection<*>, ReactiveBoolean>()

    fun <T : Any> addInspection(inspection: AppliedInspection<T>) = inspection.run {
        val problem = inspection.isProblem()
        isProblem[inspection] = problem
        if (problem.now) {
            reportingInspections.add(inspection)
            changeCount(inspection.severity, +1)
        }
        val obs = problem.observe { _, _, isProblem ->
            if (isProblem) {
                reportingInspections.add(inspection)
                changeCount(inspection.severity, +1)
            } else {
                reportingInspections.remove(inspection)
                changeCount(inspection.severity, -1)
            }
        }
        observers[inspection] = obs
    }

    fun <T : Any> removeInspection(inspection: AppliedInspection<T>) {
        val problem = isProblem.remove(inspection)!!
        observers.remove(inspection)!!.kill()
        if (problem.now) {
            reportingInspections.remove(inspection)
            changeCount(inspection.severity, -1)
        }
    }

    private fun changeCount(type: Severity, delta: Int) {
        when (type) {
            Severity.Warning -> warningCount.now += delta
            Severity.Error -> errorCount.now += delta
        }
    }

    val hasError = errorCount.notEqualTo(0)
    val hasWarning = warningCount.notEqualTo(0)

    fun problems(): Set<Problem<*>> = reportingInspections.mapTo(mutableSetOf()) { it.run { getProblem() } }

}