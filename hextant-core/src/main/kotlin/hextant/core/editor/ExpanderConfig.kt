/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.completion.Completer
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.core.Editor
import java.util.*
import kotlin.reflect.KClass

/**
 * A configuration for a [ConfiguredExpander]
 */
class ExpanderConfig<E : Editor<*>> private constructor(
    private val fallback: ExpanderDelegate<E>?,
    private val options: MutableList<ExpansionOption<E>>
) : ExpanderDelegate<E> {
    constructor(fallback: ExpanderDelegate<E>? = null) : this(fallback, mutableListOf())

    /**
     * @return a [Completer] which uses the registered choices and the given [strategy]
     */
    fun completer(strategy: CompletionStrategy): Completer<Expander<*, *>, String> =
        object : ConfiguredCompleter<Expander<*, *>, String>(strategy) {
            override fun completionPool(context: Expander<*, *>): Collection<String> = keys(context)
        }

    private fun keys(context: Expander<*, *>): Set<String> {
        val myKeys = options
            .filterIsInstance<ExpansionOption.Constant<*>>()
            .filter { it.condition(context) }
            .flatMapTo(mutableSetOf()) { it.keywords }
        return if (fallback is ExpanderConfig<*>) myKeys + fallback.keys(context) else myKeys
    }

    /**
     * Return an [ExpanderConfig] which uses the given [ExpanderDelegate] as a fallback option when expanding editors.
     */
    fun withFallback(fallback: ExpanderDelegate<E>?) =
        ExpanderConfig(fallback, options)

    /**
     * If the given [key] is expanded an editor is returned using [create].
     */
    fun registerKey(
        key: String, condition: (Expander<*, *>) -> Boolean = { true },
        create: (expander: Expander<*, *>) -> E?
    ) {
        options.add(0, ExpansionOption.Constant(setOf(key), condition, create))
    }

    /**
     * Same as [registerKey] but registers the same editor factory for multiple keys.
     */
    fun registerKeys(
        key: String, vararg more: String, condition: (Expander<*, *>) -> Boolean = { true },
        create: (expander: Expander<*, *>) -> E?
    ) {
        options.add(0, ExpansionOption.Constant(setOf(key) + more, condition, create))
    }

    /**
     * Alias for [registerKey].
     */
    fun String.expand(condition: (Expander<*, *>) -> Boolean, create: (expander: Expander<*, *>) -> E?) {
        registerKey(this, condition, create)
    }

    infix fun String.expand(create: (expander: Expander<*, *>) -> E?) {
        registerKey(this, { true }, create)
    }

    /**
     * On expanding, the given [interceptor] is invoked and if it returns a non-null value this value is returned.
     *
     * Interceptors the have been registered **last** are tried **first**
     */
    fun registerInterceptor(interceptor: (text: String, expander: Expander<*, *>) -> E?) {
        options.add(ExpansionOption.TextInterceptor(interceptor))
    }

    /**
     * Registers an interceptor that tries to to compile its given text with the given [tokenType].
     */
    fun <T : Any> registerTokenInterceptor(
        tokenType: TokenType<T?>,
        factory: (expander: Expander<*, *>, token: T) -> E
    ) {
        registerInterceptor { text: String, expander: Expander<*, *> ->
            tokenType.compile(text)?.let { t -> factory(expander, t) }
        }
    }

    /**
     * On expanding a completion item of the given class,
     * the given [interceptor] is invoked and if it returns a non-null value this value is returned.
     *
     * Interceptors the have been registered **last** are tried **first**
     */
    fun <T : Any> registerInterceptor(cls: KClass<out T>, interceptor: (item: T, expander: Expander<*, *>) -> E?) {
        @Suppress("UNCHECKED_CAST")
        options.add(ExpansionOption.CompletionInterceptor(cls, interceptor as (Expander<*, *>, Any) -> E?))
    }

    /**
     * On expanding a completion item of type [T],
     * the given [interceptor] is invoked and if it returns a non-null value this value is returned.
     *
     * Interceptors the have been registered **last** are tried **first**
     */
    @JvmName("registerTypesafeInterceptor")
    inline fun <reified T : Any> registerInterceptor(noinline interceptor: (item: T, expander: Expander<*, *>) -> E?) {
        registerInterceptor(T::class, interceptor)
    }

    /**
     * Expand the given [text] in the given [context] using the registered interceptors.
     * If no interceptor matches `null` is returned
     */
    override fun expand(text: String, expander: Expander<*, *>): E? {
        for (opt in options) {
            if (opt is ExpansionOption.Constant) {
                if (opt.keywords.contains(text) && opt.condition(expander)) {
                    val editor = opt.factory(expander)
                    if (editor != null) return editor
                }
            } else if (opt is ExpansionOption.TextInterceptor) {
                val editor = opt.factory(text, expander)
                if (editor != null) return editor
            }
        }
        if (fallback != null) return fallback.expand(text, expander)
        return null
    }

    override fun expand(item: Any, expander: Expander<*, *>): E? {
        for (opt in options) {
            if (opt is ExpansionOption.CompletionInterceptor && opt.cls.isInstance(item)) {
                val editor = opt.factory(expander, item)
                if (editor != null) return editor
            }
        }
        if (fallback != null) return fallback.expand(item, expander)
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
        options.addAll(config.options)
    }

    /**
     * Return an [ExpanderConfig] which first tries the given [config] and then uses this configuration as a fallback option.
     */
    fun extendWith(config: ExpanderConfig<E>) = config.withFallback(this)

    private sealed interface ExpansionOption<E : Editor<*>> {
        data class Constant<E : Editor<*>>(
            val keywords: Set<String>,
            val condition: (Expander<*, *>) -> Boolean,
            val factory: (Expander<*, *>) -> E?
        ) : ExpansionOption<E>

        data class TextInterceptor<E : Editor<*>>(val factory: (String, Expander<*, *>) -> E?) : ExpansionOption<E>
        data class CompletionInterceptor<E : Editor<*>>(val cls: KClass<*>, val factory: (Expander<*, *>, Any) -> E?) :
            ExpansionOption<E>
    }

    companion object {
        operator fun <E : Editor<*>> invoke(
            fallback: ExpanderConfig<E>? = null,
            build: ExpanderDelegate<E>.() -> Unit
        ) = ExpanderConfig(fallback).apply(build)
    }
}