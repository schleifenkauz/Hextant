/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import hextant.test.DESCRIBE
import hextant.test.IT
import org.jetbrains.spek.api.Spek
import reaktive.value.reactiveVariable

internal object InspectionSpec : Spek({
    val i: Inspection
    val inspected = Inspected()
    DESCRIBE("build inspection") {
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
        IT("should correctly pass the set values") {
            i.description shouldMatch equalTo("Is Ok inspection, reports inspected values that aren't ok")
        }
    }
}) {
    class Inspected {
        var isOk = reactiveVariable(false)
    }
}