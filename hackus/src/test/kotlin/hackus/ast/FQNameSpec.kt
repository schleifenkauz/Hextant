/**
 *@author Nikolaus Knop
 */

package hackus.ast

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

object FQNameSpec : Spek({
    given("a fully qualified name") {
        val jls = FQName(listOf(JIdent.of("java"), JIdent.of("lang")), JIdent.of("String"))
        on("toString") {
            val str = jls.toString()
            it("it should return the names separated by dots") {
                str shouldMatch equalTo("java.lang.String")
            }
        }
        on("toPath") {
            val p = jls.toPath()
            val parts = p.toList().map { it.toString() }
            it("should return the names separated by slashes") {
                parts shouldMatch equalTo(listOf("java", "lang", "String"))
            }
        }
    }
})