/**
 *@author Nikolaus Knop
 */

package hextant.core.util

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.throws
import hextant.impl.myLogger
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

internal object UtilSpec: Spek({
    describe("myLogger") {
        on("calling it from a non companion class") {
            class Error {
                @Suppress("unused")
                val logger by myLogger()
            }
            it("should throw an IllegalArgumentException") {
                { Error(); Unit } shouldMatch throws<IllegalStateException>()
            }
        }
        on("calling it from a companion object and getting the logger") {
            test("the logger should have the name of the class owning the companion") {
                Right.logger.name shouldMatch equalTo(Right::class.qualifiedName)
            }
        }
    }
}) {
    class Right {
        companion object {
            val logger by myLogger()
        }
    }
}