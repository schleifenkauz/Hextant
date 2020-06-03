/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Context
import hextant.Editor
import hextant.completion.*
import java.util.*

/**
 * A configuration for a [ConfiguredExpander]
 */
class ExpanderConfig<E : Editor<*>> private constructor(
    private val fallback: ExpanderDelegate<E>?,
    private val constant: MutableMap<String, (Context) -> E>,
    private val interceptors: LinkedList<(String, Context) -> E?>
) : ExpanderDelegate<E> {

    constructor(fallback: ExpanderDelegate<E>? = null) : this(fallback, mutableMapOf(), LinkedList())

    /**
     * @return a [Completer] which uses the registered choices and the given [strategy]
     */
    fun completer(strategy: CompletionStrategy): Completer<Context, String> =
        object : ConfiguredCompleter<Context, String>(strategy) {
            override fun completionPool(context: Context): Set<String> = keys()
        }

    private fun keys(): Set<String> =
        if (fallback is ExpanderConfig) constant.keys + fallback.keys() else constant.keys

    fun withFallback(fallback: ExpanderDelegate<E>?) = ExpanderConfig(fallback, constant, interceptors)

    fun <R : Editor<*>> transform(f: (E) -> R): ExpanderConfig<R> {
        val fb = if (fallback is ExpanderConfig) fallback.transform(f) else fallback?.map(f)
        val constant = constant.mapValuesTo(mutableMapOf()) { (_, fct) -> { ctx: Context -> f(fct(ctx)) } }
        val interceptors = interceptors.mapTo(LinkedList()) { fct ->
            { text: String, ctx: Context -> fct(text, ctx)?.let { f(it) } }
        }
        return ExpanderConfig(fb, constant, interceptors)
    }

    /**
     * If the given string is expanded an editor is returned using [create].
     * Previous invocation with the same [key] are overridden
     */
    fun registerConstant(key: String, create: (Context) -> E) {
        constant[key] = create
    }

    /**
     * On expanding the given [interceptor] is invoked and if it returns a non-null value this value is returned.
     * Interceptors the have been registered **last** are tried **first**
     */
    fun registerInterceptor(interceptor: (text: String, Context) -> E?) {
        interceptors.addFirst(interceptor)
    }

    /**
     * Expand the given [text] in the given [context] using the registered interceptors.
     * If no interceptor matches `null` is returned
     */
    override fun expand(text: String, context: Context): E? {
        val constant = constant[text]
        if (constant != null) return constant(context)
        for (i in interceptors) {
            val e = i(text, context)
            if (e != null) return e
        }
        if (fallback != null) return fallback.expand(text, context)
        return null
    }

    fun extend(additionalConfig: ExpanderConfig<E>.() -> Unit) = ExpanderConfig(this).apply(additionalConfig)

    fun extendWith(config: ExpanderConfig<E>) = config.withFallback(this)
}