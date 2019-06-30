/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import com.natpryce.hamkrest.absent
import hextant.test.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.nikok.kref.*
import reaktive.value.ReactiveBoolean
import reaktive.value.reactiveValue

class InspectionMemoryLeakTest {
    @Test
    fun `get problems of editor causes no memory leak`() {
        class Inspected(val error: ReactiveBoolean)

        val w = wrapper(strong(Inspected(reactiveValue(true))))
        val e by w
        val i = Inspections.newInstance()
        i.of<Inspected>().registerInspection {
            location(inspected)
            description = "An inspection"
            message { "error" }
            isSevere(true)
            preventingThat(inspected.error)
            addFix {
                description = "Fix"
                fixingBy { println(inspected) }
            }
        }
        i.getProblems(e!!)
        w.ref = weak(e!!)
        System.gc()
        e shouldBe absent()
    }
}