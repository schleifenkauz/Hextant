/**
 *@author Nikolaus Knop
 */

package hextant.core.util

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.throws
import hextant.impl.myLogger
import hextant.test.*
import org.jetbrains.spek.api.Spek

internal object UtilSpec: Spek({
    DESCRIBE("myLogger") {
        ON("calling it from a non companion class") {
            class Error {
                @Suppress("unused")
                val logger by myLogger()
            }
            IT("should throw an IllegalArgumentException") {
                { Error(); Unit } shouldMatch throws<IllegalStateException>()
            }
        }
        ON("calling it from a companion object and getting the logger") {
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