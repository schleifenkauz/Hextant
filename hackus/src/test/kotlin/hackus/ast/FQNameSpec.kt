/**
 *@author Nikolaus Knop
 */

package hackus.ast

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import hextant.err
import hextant.force
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

object FQNameSpec : Spek({
    given("a fully qualified name") {
        val jls = FQName.compile("java.lang.String").force()
        on("toString") {
            val str = jls.toString()
            it("it should return the names separated by dots") {
                str shouldMatch equalTo("java.lang.String")
            }
        }
        on("toPath with single class name") {
            val simple = FQName.compile("Clazz").force()
            val p = simple.toPath()
            it("should return the simple name") {
                p.toString() shouldMatch equalTo("Clazz")
            }
        }
        on("toPath") {
            val p = jls.toPath()
            it("should return the names separated by slashes") {
                p.toString() shouldMatch equalTo("java\\lang\\String")
            }
        }
    }
    describe("JIdent.compile") {
        on("passing an invalid java identifier") {
            it("should throw an illegal argument exception") {
                FQName.compile("123_invalid") shouldMatch equalTo(err(""))
            }
        }
        on("passing a valid java identifier") {
            val id = FQName.compile("valid_identifier123YX").force()
            it("should return an JIdent with the specified string") {
                id.toString() shouldMatch equalTo("valid_identifier123YX")
            }
        }
    }

})