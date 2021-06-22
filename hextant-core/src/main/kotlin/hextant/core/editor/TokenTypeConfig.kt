/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.completion.Completer
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.core.Editor
import java.util.*

/**
 * A [TokenType] whose token production rules can be configured.
 */
open class TokenTypeConfig<R> private constructor(
    private val default: R?,
    private val constant: MutableMap<String, R>,
    private val interceptors: LinkedList<(String) -> R?>
) : TokenType<R> {
    constructor(default: R? = null) : this(default, mutableMapOf(), LinkedList())

    constructor(default: R, init: TokenTypeConfig<R>.() -> Unit): this(default) {
        init()
    }

    constructor(init: TokenTypeConfig<R>.() -> Unit): this(null) {
        init()
    }

    /**
     * @return a [Completer] which uses the registered choices and the given [strategy]
     */
    fun completer(strategy: CompletionStrategy): Completer<Any, String> =
        object : ConfiguredCompleter<Any, String>(strategy) {
            override fun completionPool(context: Any): Collection<String> = keys()
        }

    private fun keys(): Set<String> = constant.keys

    /**
     * Return an [TokenTypeConfig] that uses the given transformation function
     * to make results of type [F] from results of type [R].
     */
    fun <F : Editor<*>> transform(f: (R) -> F): TokenTypeConfig<F> {
        val def = default?.let(f)
        val constant = constant.mapValuesTo(mutableMapOf()) { (_, r) -> f(r) }
        val interceptors = interceptors.mapTo(LinkedList()) { fct ->
            { text: String -> fct(text)?.let(f) }
        }
        return TokenTypeConfig(def, constant, interceptors)
    }

    /**
     * If the given [key] is compiled the provided [result] is returned.
     * * Previous invocation with the same [key] are overridden.
     * @see unregisterKey
     */
    fun registerKey(key: String, result: R) {
        constant[key] = result
    }

    /**
     * Same as [registerKey] but registers the same result for multiple keys.
     */
    fun registerKeys(key: String, vararg more: String, result: R) {
        registerKey(key, result)
        for (k in more) registerKey(k, result)
    }

    /**
     * Alias for [registerKey].
     */
    infix fun String.compilesTo(result: R) {
        registerKey(this, result)
    }

    /**
     * Unregisters the given [key] so that subsequent invocations of [compile]
     * will not use the previously registered factory.
     * @see registerKey
     */
    fun unregisterKey(key: String) {
        checkNotNull(constant.remove(key)) { "No factory for key '$key' registered" }
    }

    /**
     * On compiling, the given [interceptor] is invoked and if it returns a non-null value this value is returned.
     *
     * Interceptors the have been registered **last** are tried **first**
     */
    fun registerInterceptor(interceptor: (token: String) -> R?) {
        interceptors.addFirst(interceptor)
    }

    /**
     * Compile the given [token] using the registered interceptors.
     *
     * If no interceptor matches the default result is returned.
     */
    override fun compile(token: String): R {
        val constant = constant[token]
        if (constant != null) return constant
        for (i in interceptors) {
            val e = i(token)
            if (e != null) return e
        }
        return default ?: error("No interceptor matched token '$token' and no default result available")
    }
}