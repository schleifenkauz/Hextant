/**
 * @author Nikolaus Knop
 */

package matchers

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.should.shouldMatch

infix fun <T> T.shouldBe(matcher: Matcher<T>) = shouldMatch(matcher)

infix fun <T> Described<T>.shouldBe(matcher: Matcher<T>) = shouldMatch(matcher)

val `null` = Matcher<Any?>("is null") { it == null }

val `false` = equalTo(false)

val `true` = equalTo(true)