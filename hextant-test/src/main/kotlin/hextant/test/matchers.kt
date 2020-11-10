/**
 * @author Nikolaus Knop
 */

package hextant.test

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.MatchResult.Match
import com.natpryce.hamkrest.MatchResult.Mismatch
import com.natpryce.hamkrest.should.shouldMatch
import kotlin.reflect.KClass

/**
 * Aliases for [shouldMatch]
 */
infix fun <T> T.shouldBe(matcher: Matcher<T>) = shouldMatch(matcher)

infix fun <T> Described<T>.shouldBe(matcher: Matcher<T>) = shouldMatch(matcher)

infix fun <T> T.shouldEqual(value: T) = this shouldBe equalTo(value)

inline infix fun <reified E : Throwable> (() -> Unit).shouldThrow(exceptionMatcher: Matcher<E>?) {
    { this(); Unit } shouldMatch throws(exceptionMatcher)
}


//Exception matchers
inline fun <reified E : Throwable> (() -> Unit).shouldThrow(): Unit = shouldThrow<E>(null)

@JvmName("shouldThrowAny")
inline infix fun <reified E : Throwable> (() -> Any?).shouldThrow(exceptionMatcher: Matcher<E>?): Unit =
    { this(); Unit } shouldThrow (exceptionMatcher)

@JvmName("shouldThrowAny")
inline fun <reified E : Throwable> (() -> Any?).shouldThrow(): Unit = shouldThrow<E>(null)

//Common matchers
val `null` = Matcher<Any?>("is null") { it == null }

val `false` = equalTo(false)

val `true` = equalTo(true)

val isEmpty = Matcher(Collection<Any?>::isEmpty)

fun <T : Any> instanceOf(cls: KClass<out T>): Matcher<Any> =
    Matcher("A value that is instance of $cls") { cls.isInstance(it) }

inline fun <reified T : Any> instanceOf() = instanceOf(T::class)

fun <E> contains(element: E): Matcher<Collection<E?>> = Matcher("contains $element") { it.contains(element) }

fun <E> aSetOf(vararg elementMatchers: Matcher<E>) = object : Matcher.Primitive<Set<E>>() {
    override val description: String = buildString {
        append("contains all these elements: ")
        elementMatchers.joinTo(this, ",\n") { "A value that ${it.description}" }
    }

    override fun invoke(actual: Set<E>): MatchResult {
        val expectedSize = elementMatchers.size
        val actualSize = actual.size
        if (expectedSize != actualSize) {
            return Mismatch("got $actualSize elements instead of $expectedSize")
        }
        val copy = actual.toMutableSet()
        outer@ for (m in elementMatchers) {
            val itr = copy.iterator()
            while (itr.hasNext()) { //Cannot use removeIf or removeAll because only one element should be removed
                val e = itr.next()
                if (m.asPredicate().invoke(e)) {
                    itr.remove()
                    continue@outer
                }
            }
            return Mismatch("No element found that satisfies $m")
        }
        return Match
    }
}

fun <E> aListOf(vararg elementMatchers: Matcher<E>) = object : Matcher.Primitive<List<E>>() {
    override val description: String = buildString {
        append("contains all these elements in order: ")
        elementMatchers.joinTo(this, ",\n") { "A value that ${it.description}" }
    }

    override fun invoke(actual: List<E>): MatchResult {
        val expectedSize = elementMatchers.size
        val actualSize = actual.size
        if (expectedSize != actualSize) {
            return Mismatch("got $actualSize elements instead of $expectedSize")
        }
        elementMatchers.withIndex().zip(actual) { (i, m), e ->
            val result = m(e)
            if (result is Mismatch) {
                return Mismatch("Mismatch at index $i: $result")
            }
        }
        return Match
    }
}