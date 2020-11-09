/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import reaktive.dependencies
import reaktive.value.ReactiveValue
import reaktive.value.binding.binding
import reaktive.value.now
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

internal fun <E : Any> KClass<E>.getSimpleEditorConstructor(): (Context) -> E {
    val cstr = this.constructors.find { cstr ->
        val params = cstr.parameters.filter { !it.isOptional }
        params.size == 1 && params[0].type.classifier == Context::class
    } ?: throw NoSuchMethodException("$qualifiedName.<init>(hextant.context.Context)")
    cstr.isAccessible = true
    val param = cstr.parameters.find { it.type.classifier == Context::class }!!
    return { ctx: Context -> cstr.callBy(mapOf(param to ctx)) }
}

@PublishedApi internal fun <T : Any> List<T?>.takeIfNoNulls(): List<T>? = map { it ?: return null }

internal fun Any.getTypeArgument(superclass: KClass<*>, index: Int): KType {
    val supertype = this::class.allSupertypes.first { it.classifier == superclass }
    return supertype.arguments[index].type!!
}

internal fun launchSynchronized(mutex: Mutex, action: suspend () -> Unit) {
    GlobalScope.launch(Dispatchers.Main) {
        mutex.withLock { action() }
    }
}

@Suppress("UNCHECKED_CAST")
@PublishedApi internal inline fun <R> implComposeResult(
    resultClass: KClass<*>,
    checkNullComponents: Boolean,
    crossinline default: () -> R,
    vararg components: Editor<*>
): ReactiveValue<R> {
    val results = components.map { it.result }
    val cstr = resultClass.primaryConstructor ?: error("$resultClass has no primary constructor")
    return binding<R>(dependencies(results)) {
        val comps = results.map { it.now }
        if (checkNullComponents) for (c in comps) if (c !== null) return@binding default()
        cstr.call(*comps.toTypedArray()) as R
    }
}

/**
 * Compose a result from the given editor [components] by taking the results of the editors
 * and passing them to the primary constructor of the class [R].
 */
inline fun <reified R> composeResult(vararg components: Editor<*>): ReactiveValue<R> =
    implComposeResult(R::class, false, { throw AssertionError() }, *components)

/**
 * Compose a result from the given editor [components] by taking the results of the editors
 * and passing them to the primary constructor of the class [R].
 * If any of the component results is null the [default] result is used.
 */
inline fun <reified R> composeResult(vararg components: Editor<*>, crossinline default: () -> R): ReactiveValue<R> =
    implComposeResult(R::class, true, default, *components)

/**
 * Compose a result from the given editor [components] by taking the results of the editors
 * and passing them to the primary constructor of the class [R].
 * If any of the component results is null the composed result is also null.
 */
inline fun <reified R : Any> composeResultNullable(vararg components: Editor<*>): ReactiveValue<R?> =
    composeResult(*components) { null }

