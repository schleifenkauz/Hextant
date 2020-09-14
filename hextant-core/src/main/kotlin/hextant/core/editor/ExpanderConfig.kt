/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.completion.*
import hextant.context.Context
import hextant.core.Editor
import validated.map
import validated.orNull
import java.util.*
import kotlin.reflect.KClass

/**
 * A configuration for a [ConfiguredExpander]
 */
class ExpanderConfig<E : Editor<*>> private constructor(
    private val fallback: ExpanderDelegate<E>?,
    private val constant: MutableMap<String, (Context) -> E>,
    private val interceptors: LinkedList<(String, Context) -> E?>,
    private val typeSafeInterceptors: MutableMap<KClass<*>, LinkedList<(Any, Context) -> E?>>
) : ExpanderDelegate<E> {
    constructor(fallback: ExpanderDelegate<E>? = null) : this(fallback, mutableMapOf(), LinkedList(), mutableMapOf())

    /**
     * @return a [Completer] which uses the registered choices and the given [strategy]
     */
    fun completer(strategy: CompletionStrategy): Completer<Context, String> =
        object : ConfiguredCompleter<Context, String>(strategy) {
            override fun completionPool(context: Context): Set<String> = keys()
        }

    private fun keys(): Set<String> =
        if (fallback is ExpanderConfig) constant.keys + fallback.keys() else constant.keys

    /**
     * Return an [ExpanderConfig] which uses the given [ExpanderDelegate] as a fallback option when expanding editors.
     */
    fun withFallback(fallback: ExpanderDelegate<E>?) =
        ExpanderConfig(fallback, constant, interceptors, typeSafeInterceptors)

    /**
     * Return an [ExpanderConfig] that uses the given transformation function to make editors of type [R] from editors of type [E].
     */
    fun <R : Editor<*>> transform(f: (E) -> R): ExpanderConfig<R> {
        val fb = if (fallback is ExpanderConfig) fallback.transform(f) else fallback?.map(f)
        val constant = constant.mapValuesTo(mutableMapOf()) { (_, fct) -> { ctx: Context -> f(fct(ctx)) } }
        val interceptors = interceptors.mapTo(LinkedList()) { fct ->
            { text: String, ctx: Context -> fct(text, ctx)?.let(f) }
        }
        val typeSafeInterceptors = typeSafeInterceptors.mapValuesTo(mutableMapOf()) { (_, lst) ->
            lst.mapTo(LinkedList()) { fct ->
                { item: Any, ctx: Context -> fct(item, ctx)?.let(f) }
            }
        }
        return ExpanderConfig(fb, constant, interceptors, typeSafeInterceptors)
    }

    /**
     * If the given string is expanded an editor is returned using [create].
     * Previous invocation with the same [key] are overridden
     */
    fun registerConstant(key: String, create: (ctx: Context) -> E) {
        constant[key] = create
    }

    /**
     * Alias for [registerConstant].
     */
    operator fun String.plusAssign(create: (ctx: Context) -> E) {
        registerConstant(this, create)
    }

    /**
     * On expanding, the given [interceptor] is invoked and if it returns a non-null value this value is returned.
     *
     * Interceptors the have been registered **last** are tried **first**
     */
    fun registerInterceptor(interceptor: (text: String, ctx: Context) -> E?) {
        interceptors.addFirst(interceptor)
    }

    /**
     * Registers an interceptor that tries to to compile its given text with the given [tokenType].
     */
    fun <T> registerTokenInterceptor(tokenType: TokenType<T>, factory: (ctx: Context, token: T) -> E) {
        registerInterceptor { text: String, ctx: Context ->
            tokenType.compile(text).map { t -> factory(ctx, t) }.orNull()
        }
    }

    /**
     * On expanding a completion item of the given class,
     * the given [interceptor] is invoked and if it returns a non-null value this value is returned.
     *
     * Interceptors the have been registered **last** are tried **first**
     */
    fun <T : Any> registerInterceptor(cls: KClass<out T>, interceptor: (item: T, ctx: Context) -> E?) {
        val list = typeSafeInterceptors.getOrPut(cls) { LinkedList() }
        @Suppress("UNCHECKED_CAST")
        list.addFirst(interceptor as ((Any, Context) -> E?)?)
    }

    /**
     * On expanding a completion item of type [T],
     * the given [interceptor] is invoked and if it returns a non-null value this value is returned.
     *
     * Interceptors the have been registered **last** are tried **first**
     */
    @JvmName("registerTypesafeInterceptor")
    inline fun <reified T : Any> registerInterceptor(noinline interceptor: (item: T, ctx: Context) -> E?) {
        registerInterceptor(T::class, interceptor)
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

    override fun expand(item: Any, context: Context): E? {
        val cls = item::class
        val interceptors = typeSafeInterceptors[cls] ?: return null
        for (i in interceptors) {
            val e = i(item, context)
            if (e != null) return e
        }
        if (fallback != null) return fallback.expand(item, context)
        return null
    }

    /**
     * Return an [ExpanderConfig] which uses this config as a fallback option and apply the [additionalConfig] block.
     */
    fun extend(additionalConfig: ExpanderConfig<E>.() -> Unit) = ExpanderConfig(this).apply(additionalConfig)

    /**
     * Copies all the constant factories and interceptors from the given [config] to this one.
     */
    fun alsoUse(config: ExpanderConfig<E>) {
        constant.putAll(config.constant)
        interceptors.addAll(config.interceptors)
        for ((cls, interceptors) in config.typeSafeInterceptors) {
            typeSafeInterceptors.getOrPut(cls) { LinkedList() }.addAll(interceptors)
        }
    }

    /**
     * Return an [ExpanderConfig] which first tries the given [config] and then uses this configuration as a fallback option.
     */
    fun extendWith(config: ExpanderConfig<E>) = config.withFallback(this)
}