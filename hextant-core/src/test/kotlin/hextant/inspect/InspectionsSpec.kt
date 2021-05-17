package hextant.inspect

import com.natpryce.hamkrest.should.shouldMatch
import hextant.inspect.Problem.Severity
import hextant.test.isEmpty
import hextant.test.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.ActionBody
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import reaktive.value.ReactiveBoolean
import reaktive.value.binding.greaterThan
import reaktive.value.binding.map
import reaktive.value.binding.not
import reaktive.value.now
import reaktive.value.reactiveVariable

internal object InspectionsSpec : Spek({
    given("inspections") {
        val inspections = Inspections.newInstance()
        val inspected = Inspected()
        fun ActionBody.checkNoErrors() {
            it("should report no error") {
                inspections.hasError(inspected).now shouldEqual false
            }
        }

        fun ActionBody.checkNoWarning() {
            it("should have no warnings") {
                inspections.hasWarning(inspected).now shouldEqual false
            }
        }

        fun ActionBody.checkNoProblems() {
            checkNoWarning()
            checkNoErrors()
            it("should have no problems") {
                inspections.getProblems(inspected) shouldMatch isEmpty
            }
        }
        on("initially") {
            checkNoProblems()
        }
        on("registering an inspection that doesn't report a problem initially") {
            inspections.registerInspection<Inspected> {
                id = "even"
                description = "Prevents even integers"
                isSevere(false)
                message { "${inspected.number.now} is even" }
                preventingThat { inspected.number.map { n -> n % 2 == 0 } }
            }
            checkNoProblems()
        }
        on("changing the inspected object such that it is reported") {
            inspected.number.set(2) //even number
            it("should report a warning") {
                inspections.hasWarning(inspected).now shouldEqual true
            }
            checkNoErrors()
            it("should report one problem") {
                inspections.getProblems(inspected).map { it.severity } shouldEqual listOf(Severity.Warning)
            }
        }
        on("changing the inspected object such that it is not reported anymore") {
            inspected.number.set(3) //odd even
            checkNoProblems()
        }
        on("registering another inspection for a superclass and making the inspected object reportable") {
            inspected.number.set(0) //even and non-positive
            inspections.registerInspection<IInspected> {
                id = "non-positive"
                description = "Prevents non-positive integers"
                isSevere(true)
                message { "Number is non-positive" }
                preventingThat { !inspected.isPositive }
            }
            it("should have an error") {
                inspections.hasError(inspected).now shouldEqual true
            }
            it("should have a warning") {
                inspections.hasWarning(inspected).now shouldEqual true
            }
            it("should report both a warning and problem") {
                val severities = inspections.getProblems(inspected).mapTo(mutableSetOf()) { it.severity }
                severities shouldEqual setOf(Severity.Warning, Severity.Error)
            }
        }
        on("making the inspected object unproblematic again") {
            inspected.number.set(5)
            checkNoProblems()
        }
    }
}) {
    interface IInspected {
        val isPositive: ReactiveBoolean
    }

    class Inspected : IInspected {
        val number = reactiveVariable(1)

        override val isPositive: ReactiveBoolean = number.greaterThan(0)
    }
}