/**
 *@author Nikolaus Knop
 */

package hextant

import reaktive.dependencies
import reaktive.value.ReactiveValue
import reaktive.value.binding.binding
import reaktive.value.binding.map
import reaktive.value.now

/**
 * The result of an [Editor]
 */
sealed class CompileResult<out T>

/**
 * An error directly located in the [Editor]
 * @property message the message of this error
 */
data class Err(val message: String) : CompileResult<Nothing>()

/**
 * Anything is ok
 * @property value the value
 */
data class Ok<T>(val value: T) : CompileResult<T>()

/**
 * @return an [Ok] result with the given [value]
 */
fun <T> ok(value: T): CompileResult<T> = Ok(value)

/**
 * @return an [Err] with the given [message]
 */
fun <T> err(message: String): CompileResult<T> = Err(message)

/**
 * @return a [ChildErr]
 */
fun <T> childErr(): CompileResult<T> = ChildErr

/**
 * An error located in one of the children of the [Editor]
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
 * If this result is an error return the result of [alternative], otherwise return this Result
 */
inline fun <T> CompileResult<T>.orElse(alternative: () -> CompileResult<T>): CompileResult<T> = when (this) {
    is Ok -> this
    else  -> alternative()
}

/**
 * If this result is an error return [alternative], otherwise return this Result
 */
fun <T> CompileResult<T>.or(alternative: CompileResult<T>): CompileResult<T> = orElse { alternative }

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
 * Execute the given action with the value of this result if it is ok
 */
inline fun <T> CompileResult<T>.ifOk(action: (T) -> Unit) {
    if (this is Ok) action(value)
}

/**
 * Execute the given action on the values of the given results if both are ok
 */
inline fun <T, R> ifOk(t: CompileResult<T>, r: CompileResult<R>, action: (T, R) -> Unit) {
    if (t is Ok && r is Ok) action(t.value, r.value)
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
 * @property err the error the computation terminated with
 */
class TerminationSignal(val err: CompileResult<Nothing>) : Throwable()

/**
 * Return the value of this [CompileResult] if it is [Ok], otherwise throw a [TerminationSignal]
 */
inline fun <T> CompileResult<T>.orTerminate(result: (CompileResult<Nothing>) -> CompileResult<Nothing> = { it }): T =
    ifErr { throw TerminationSignal(result(it)) }

/**
 * Syntactic sugar for [orTerminate]
 */
operator fun <T> CompileResult<T>.component1(): T = orTerminate()

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

/**
 * Syntactic sugar for `map { it.map(f) }`
 */
fun <T, F> EditorResult<T>.mapResult(f: (T) -> F) = map { it.map(f) }

/**
 * @return a binding which has the results of the given editors as dependencies and computes [body]
 */
fun <T> result(vararg deps: Editor<*>, body: () -> CompileResult<T>): EditorResult<T> =
    binding(dependencies(deps.map { it.result }), body)

/**
 * @return an [EditorResult] with the given dependencies computing [body]
 */
fun <A : Any, T> result1(dep1: Editor<A>, body: (A) -> CompileResult<T>): EditorResult<T> = result(dep1) {
    compile {
        body(dep1.result.now.orTerminate { childErr() })
    }
}

/**
 * @return an [EditorResult] with the given dependencies computing [body]
 */
fun <A : Any, B : Any, T> result2(dep1: Editor<A>, dep2: Editor<B>, body: (A, B) -> CompileResult<T>): EditorResult<T> =
    result(dep1, dep2) {
        compile {
            body(dep1.result.now.orTerminate { childErr() }, dep2.result.now.orTerminate { childErr() })
        }
    }

/**
 * @return an [EditorResult] with the given dependencies computing [body]
 */
fun <A : Any, B : Any, C : Any, T> result3(
    dep1: Editor<A>,
    dep2: Editor<B>,
    dep3: Editor<C>,
    body: (A, B, C) -> CompileResult<T>
): EditorResult<T> =
    result(dep1, dep2, dep3) {
        compile {
            body(
                dep1.result.now.orTerminate { childErr() },
                dep2.result.now.orTerminate { childErr() },
                dep3.result.now.orTerminate { childErr() }
            )
        }
    }


/**
 * The result of an [Editor] is a reactive value of compile results
 */
typealias EditorResult<R> = ReactiveValue<CompileResult<R>>