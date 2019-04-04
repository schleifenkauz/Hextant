/**
 *@author Nikolaus Knop
 */

package hextant

import reaktive.value.ReactiveValue
import kotlin.reflect.KProperty

sealed class CompileResult<out T>

data class Err(val message: String) : CompileResult<Nothing>()

data class Ok<T>(val value: T) : CompileResult<T>()

object ChildErr : CompileResult<Nothing>()

val CompileResult<*>.isOk get() = this is Ok

inline fun <T, R> CompileResult<T>.map(f: (T) -> R): CompileResult<R> = when (this) {
    is Err      -> this
    is ChildErr -> this
    is Ok       -> Ok(f(value))
}

inline fun <T, R> CompileResult<T>.flatMap(f: (T) -> CompileResult<R>): CompileResult<R> = when (this) {
    is Err      -> this
    is ChildErr -> this
    is Ok       -> f(value)
}

fun <T> CompileResult<T>.or(alternative: CompileResult<T>): CompileResult<T> = when (this) {
    is Ok -> this
    else  -> alternative
}

inline fun <T> CompileResult<T>.orElse(alternative: () -> CompileResult<T>): CompileResult<T> = when (this) {
    is Ok -> this
    else  -> alternative()
}

inline fun <T> T?.okOr(err: () -> CompileResult<T>): CompileResult<T> = if (this != null) Ok(this) else err()

inline fun <T> T?.okOrErr(message: () -> String): CompileResult<T> = okOr { Err(message()) }

fun <T> T?.okOrChildErr(): CompileResult<T> = okOr { ChildErr }

inline fun <T> T.okIfOrErr(predicate: Boolean, message: () -> String) = if (predicate) Ok(this) else Err(message())

inline fun <T> T.okIfOrChildErr(predicate: (T) -> Boolean) = if (predicate(this)) Ok(this) else ChildErr

fun <T> CompileResult<T>.force() = if (this is Ok) value else throw IllegalArgumentException("Attempt to force $this")

inline fun <T, F> CompileResult<T>.mapOrChildErr(f: (T) -> F) = if (this is Ok) Ok(f(value)) else ChildErr

inline fun <T> CompileResult<T>.default(def: () -> T): T = if (this is Ok) value else def()

fun <T> CompileResult<T>.toChildErr() = if (isOk) this else ChildErr

fun <T> CompileResult<T>.defaultNull() = default { null }

val CompileResult<*>.isError get() = this !is Ok

class ErrException(val err: CompileResult<Nothing>) : Exception()

operator fun <T> CompileResult<T>.getValue(receiver: Any?, property: KProperty<*>): T = when (this) {
    is Err      -> throw ErrException(this)
    is ChildErr -> throw ErrException(this)
    is Ok       -> value
}

inline fun <T> mdo(body: () -> CompileResult<T>): CompileResult<T> {
    return try {
        body()
    } catch (interrupt: ErrException) {
        interrupt.err
    }
}

typealias RResult<R> = ReactiveValue<CompileResult<R>>