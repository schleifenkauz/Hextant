/**
 *@author Nikolaus Knop
 */

package hextant

import reaktive.value.ReactiveValue
import kotlin.reflect.KProperty

/**
 * The result of an [Editable]
 */
sealed class CompileResult<out T>

/**
 * An error directly located in the [Editable]
 */
data class Err(val message: String) : CompileResult<Nothing>()

/**
 * Anything is ok
 */
data class Ok<T>(val value: T) : CompileResult<T>()

/**
 * An error located in one of the children of the [Editable]
 */
object ChildErr : CompileResult<Nothing>()

/**
 * Return `true` if this result is [Ok]
 */
val CompileResult<*>.isOk get() = this is Ok

/**
 * Return `true` if the result is either an [Err] or a [ChildErr]
 */
val CompileResult<*>.isError get() = !isOk

/**
 * Return `true` if the result is an [Err]
 */
val CompileResult<*>.isErr get() = this is Err

/**
 * Return `true` if the result is a [ChildErr]
 */
val CompileResult<*>.isChildErr get() = this == ChildErr

/**
 * If the result is ok than return an [Ok] result mapping the value with [f], else return this error
 */
inline fun <T, R> CompileResult<T>.map(f: (T) -> R): CompileResult<R> = when (this) {
    is Err      -> this
    is ChildErr -> this
    is Ok       -> Ok(f(value))
}

/**
 * If the result is ok than return [f] applied to the value, else return this error
 */
inline fun <T, R> CompileResult<T>.flatMap(f: (T) -> CompileResult<R>): CompileResult<R> = when (this) {
    is Err      -> this
    is ChildErr -> this
    is Ok       -> f(value)
}

/**
 * If this result is an error return [alternative], otherwise return this Result
 */
fun <T> CompileResult<T>.or(alternative: CompileResult<T>): CompileResult<T> = when (this) {
    is Ok -> this
    else  -> alternative
}

/**
 * If this result is an error return the result of [alternative], otherwise return this Result
 */
inline fun <T> CompileResult<T>.orElse(alternative: () -> CompileResult<T>): CompileResult<T> = when (this) {
    is Ok -> this
    else  -> alternative()
}

/**
 * If this value is `null` return the [default], otherwise return an [Ok] wrapping this non-null value
 */
inline fun <T> T?.okOr(default: () -> CompileResult<T>): CompileResult<T> = if (this != null) Ok(this) else default()

/**
 * If this value is `null` return an [Err] with the specified [message], otherwise return an [Ok] wrapping this non-null value
 * * Identical to `okOr { Err(message()) }`
 */
inline fun <T> T?.okOrErr(message: () -> String): CompileResult<T> = okOr { Err(message()) }

/**
 * If this value is `null` return a [ChildErr], otherwise return an [Ok] wrapping this non-null value
 * * Identical to `okOr { ChildErr }`
 */
fun <T> T?.okOrChildErr(): CompileResult<T> = okOr { ChildErr }

/**
 * If this result is [Ok] than return the value, otherwise return the result of applying [def] to this error
 */
inline fun <T> CompileResult<T>.ifErr(def: (CompileResult<Nothing>) -> T): T = when (this) {
    is Err      -> def(this)
    is ChildErr -> def(this)
    is Ok       -> value
}

/**
 * If this result is [Ok] than return the value, otherwise throw an [IllegalArgumentException]
 */
fun <T> CompileResult<T>.force() = ifErr { error -> throw IllegalArgumentException("Attempt to force $error") }

/**
 * If this result is [Ok] than return the value, otherwise return `null`
 */
fun <T> CompileResult<T>.orNull() = ifErr { null }

/**
 * Throwable signaling that the computation should terminate with the specified [err]
 */
class TerminationSignal(val err: CompileResult<Nothing>) : Throwable()

/**
 * Return the value of this [CompileResult] if it is [Ok], otherwise throw a [TerminationSignal]
 */
operator fun <T> CompileResult<T>.getValue(receiver: Any?, property: KProperty<*>): T = when (this) {
    is Err      -> throw TerminationSignal(this)
    is ChildErr -> throw TerminationSignal(this)
    is Ok       -> value
}

/**
 * Execute the specified [body] and return the result catching an eventual [TerminationSignal] thrown by the body
 */
inline fun <T> compile(body: () -> CompileResult<T>): CompileResult<T> {
    return try {
        body()
    } catch (interrupt: TerminationSignal) {
        interrupt.err
    }
}

typealias RResult<R> = ReactiveValue<CompileResult<R>>