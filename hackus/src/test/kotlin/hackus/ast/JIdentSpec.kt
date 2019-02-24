/**
 *@author Nikolaus Knop
 */

package hackus.ast

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.should.shouldMatch
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

object JIdentSpec : Spek({
    describe("creation") {
        describe("JIdent.of") {
            on("passing an invalid java identifier") {
                it("should throw an illegal argument exception") {
                    { JIdent.of("123_invalid"); Unit } shouldMatch throws<IllegalArgumentException>()
                }
            }
            on("passing a valid java identifier") {
                val id = JIdent.of("valid_identifier123YX")
                it("should return an JIdent with the specified string") {
                    id.toString() shouldMatch equalTo("valid_identifier123YX")
                }
            }
        }
        describe("JIdent.orNull") {
            on("passing an invalid java identifier") {
                val id = JIdent.orNull("xyz/abc")
                it("should return null") {
                    id shouldMatch absent()
                }
            }
            on("passing a valid java identifier") {
                val id = JIdent.orNull("valid_java")
                it("should just return this identifier") {
                    id.toString() shouldMatch equalTo("valid_java")
                }
            }
        }
        describe("JIdent.isValid") {
            val testCases = listOf(
                "1ab" to false,
                "" to false,
                "abc" to true,
                "_abc" to true,
                "ABC123" to true,
                "abc$123" to false,
                "abc." to false,
                "ABCDEF" to true
            )
            for ((str, valid) in testCases) {
                action("is $str a valid java identifier?") {
                    val actual = JIdent.isValid(str)
                    if (valid) {
                        it("should be valid") {
                            actual shouldMatch equalTo(true)
                        }
                    } else {
                        it("should not be valid") {
                            actual shouldMatch equalTo(false)
                        }
                    }
                }
            }
        }
    }
    given("a JIdent") {
        val id = JIdent.of("identifier123")
        on("toString") {
            val str = id.toString()
            it("should return the name") {
                str shouldMatch equalTo("identifier123")
            }
        }
        on("hashCode") {
            val hash = id.hashCode()
            it("should return that of the string") {
                val expectedHash = "identifier123".hashCode()
                hash shouldMatch equalTo(expectedHash)
            }
        }
        on("equals JIdent with other name") {
            val other = JIdent.of("otherName")
            val equal = id == other
            it("should return false") {
                equal shouldMatch equalTo(false)
            }
        }
        on("equals JIdent with same name") {
            val other = JIdent.of("identifier123")
            val equal = id == other
            it("should return true") {
                equal shouldMatch equalTo(true)
            }
        }
        on("equals other object") {
            val other = Any()
            val equal = id == other
            it("should return false") {
                equal shouldMatch equalTo(false)
            }
        }
    }
})