package hextant.inspect

import com.natpryce.hamkrest.should.shouldMatch
import hextant.inspect.Problem.Error
import hextant.inspect.Problem.Warning
import hextant.test.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.ActionBody
import reaktive.value.*
import reaktive.value.binding.*

internal object InspectionsSpec : Spek({
    GIVEN("inspections") {
        val inspections = Inspections.newInstance()
        val inspected = Inspected()
        fun ActionBody.checkNoErrors() {
            IT("should report no error") {
                inspections.hasError(inspected).now shouldEqual false
            }
        }

        fun ActionBody.checkNoWarning() {
            IT("should have no warnings") {
                inspections.hasWarning(inspected).now shouldEqual false
            }
        }

        fun ActionBody.checkNoProblems() {
            checkNoWarning()
            checkNoErrors()
            IT("should have no problems") {
                inspections.getProblems(inspected) shouldMatch isEmpty
            }
        }
        ON("initially") {
            checkNoProblems()
        }
        ON("registering an inspection that doesn't report a problem initially") {
            inspections.of<Inspected>().registerInspection {
                description = "Prevents even integers"
                isSevere(false)
                message { "${inspected.number.now} is even" }
                preventingThat(inspected.number.map { n -> n % 2 == 0 })
            }
            checkNoProblems()
        }
        ON("changing the inspected object such that it is reported") {
            inspected.number.set(2) //even number
            IT("should report a warning") {
                inspections.hasWarning(inspected).now shouldEqual true
            }
            checkNoErrors()
            IT("should report one problem") {
                inspections.getProblems(inspected) shouldBe aSetOf(instanceOf<Warning>())
            }
        }
        ON("changing the inspected object such that it is not reported anymore") {
            inspected.number.set(3) //odd even
            checkNoProblems()
        }
        ON("registering another inspection for a superclass and making the inspected object reportable") {
            inspected.number.set(0) //even and non-positive
            inspections.of<IInspected>().registerInspection {
                description = "Prevents non-positive integers"
                isSevere(true)
                message { "Number is non-positive" }
                preventingThat(!inspected.isPositive)
            }
            IT("should have an error") {
                inspections.hasError(inspected).now shouldEqual true
            }
            IT("should have a warning") {
                inspections.hasWarning(inspected).now shouldEqual true
            }
            IT("should report both a warning and problem") {
                inspections.getProblems(inspected) shouldBe aSetOf(
                    instanceOf<Warning>(),
                    instanceOf<Error>()
                )
            }
        }
        ON("making the inspected object unproblematic again") {
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