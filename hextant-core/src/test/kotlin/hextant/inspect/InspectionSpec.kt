/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import reaktive.value.reactiveVariable

internal object InspectionSpec: Spek({
    lateinit var i: Inspection<Inspected>
    val inspected = Inspected()
    describe("build inspection") {
        i = inspection(inspected) {
            description = "Is Ok inspection, reports inspected values that aren't ok"
            message { "$inspected is not OK" }
            isSevere(true)
            checkingThat(inspected.isOk)
            addFix {
                description = "Make inspected ok"
                fixingBy {
                    inspected.isOk.set(true)
                }
            }
        }
        it("should correctly pass the set values") {
            i.description shouldMatch equalTo("Is Ok inspection, reports inspected values that aren't ok")
            i.inspected shouldMatch equalTo(inspected)
        }
    }
}) {
    class Inspected {
        var isOk = reactiveVariable(false)
    }
}