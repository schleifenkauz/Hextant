/**
 * @author Nikolaus Knop
 */

package matchers

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.should.shouldMatch
import hextant.*
import hextant.core.instanceOf

infix fun <T> T.shouldBe(matcher: Matcher<T>) = shouldMatch(matcher)

infix fun <T> Described<T>.shouldBe(matcher: Matcher<T>) = shouldMatch(matcher)

infix fun <T> T.shouldEqual(value: T) = this shouldBe equalTo(value)

inline infix fun <reified E : Throwable> (() -> Unit).shouldThrow(exceptionMatcher: Matcher<E>?) {
    { this(); Unit } shouldMatch throws(exceptionMatcher)
}

inline fun <reified E : Throwable> (() -> Unit).shouldThrow(): Unit = shouldThrow<E>(null)

@JvmName("shouldThrowAny")
inline infix fun <reified E : Throwable> (() -> Any?).shouldThrow(exceptionMatcher: Matcher<E>?): Unit =
    { this(); Unit } shouldThrow (exceptionMatcher)

@JvmName("shouldThrowAny")
inline fun <reified E : Throwable> (() -> Any?).shouldThrow(): Unit = shouldThrow<E>(null)

val `null` = Matcher<Any?>("is null") { it == null }

val `false` = equalTo(false)

val `true` = equalTo(true)

val isEmpty = Matcher(Collection<Any?>::isEmpty)

fun <E> contains(element: E): Matcher<Collection<E?>> = Matcher("contains $element") { it.contains(element) }

val error = Matcher(CompileResult<*>::isError)

val err: Matcher<CompileResult<*>> = instanceOf<Err>()

val childErr: Matcher<CompileResult<*>> = equalTo(ChildErr)

val ok: Matcher<CompileResult<*>> = instanceOf<Ok<*>>()