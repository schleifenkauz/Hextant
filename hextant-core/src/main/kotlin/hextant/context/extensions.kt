package hextant.context

import bundles.*
import hextant.base.EditorSnapshot
import hextant.core.Editor
import hextant.fx.EditorControl
import hextant.serial.SerialProperties
import kserial.*
import java.nio.file.Path
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Synonym for [apply]
 */
inline fun EditorControlFactory.configure(config: EditorControlFactory.() -> Unit) {
    apply(config)
}

/**
 * Syntactic sugar for `register(typeOf<T>(), factory)`
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> EditorFactory.register(noinline factory: (Context, T) -> Editor<T>) {
    @Suppress("UNCHECKED_CAST")
    register(typeOf<T>(), factory as (Context, Any?) -> Editor<Any?>)
}

/**
 * Syntactic sugar for `register(typeOf<T>(), factory)`
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> EditorFactory.register(noinline factory: (Context) -> Editor<T>) {
    register(typeOf<T>(), factory)
}

/**
 * Syntactic sugar for `createEditor(typeOf<T>(), context)`
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> EditorFactory.createEditor(context: Context) = createEditor(
    typeOf<T>(), context
)

/**
 * Syntactic sugar for `createEditor(typeOf<T>(), result, context)`
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> EditorFactory.createEditor(result: T, context: Context) =
    createEditor(typeOf<T>(), result, context)

/**
 * Typesafe version of [Editor.createSnapshot]
 */
@Suppress("UNCHECKED_CAST")
fun <E : Editor<*>> E.snapshot(): EditorSnapshot<E> = createSnapshot() as EditorSnapshot<E>

/**
 *
 */
fun <T, Read : Any, Write : Read> Context.replace(permission: Write, property: Property<T, Read, Write>, value: T) {
    when {
        hasProperty(permission, property) -> set(permission, property, value)
        parent != null                    -> parent!!.replace(permission, property, value)
        else                              -> throw NoSuchElementException("Property $property not configured")
    }
}

/**
 * Uses the [EditorControlGroup] of this [Context] to create a view for the given [editor] with the specified [arguments]
 */
fun Context.createView(editor: Editor<*>, arguments: Bundle = createBundle()): EditorControl<*> {
    val group = get(EditorControlGroup)
    return group.createViewFor(editor, this, arguments)
}

/**
 * Create a view for the given [editor]. The [configure]-block is used to initialize the [hextant.core.EditorView.arguments].
 */
inline fun Context.createView(editor: Editor<*>, configure: Bundle.() -> Unit): EditorControl<*> =
    createView(editor, createBundle(configure))

/**
 * Uses the [EditorFactory] of this [Context] to create an [Editor] with the given [resultType].
 * If this [Context] has no [EditorFactory] or there isn't an editor registered in the [EditorFactory],
 * this method recursively tries to create an [Editor] with the [Context.parent] [Context].
 * This method is covariant in the sense that it could also create an [Editor] for results of type `S` if `S` is a subtype of `R`.
 * @throws NoSuchElementException if neither this context nor any of its parent contexts can create an [Editor]
 * for the given result type.
 */
fun <R> Context.createEditor(resultType: KType): Editor<R> =
    try {
        @Suppress("UNCHECKED_CAST")
        get(EditorFactory).createEditor(resultType, this) as Editor<R>
    } catch (e: NoSuchElementException) {
        parent?.createEditor(resultType) ?: throw e
    }

/**
 * Syntactic sugar for createEditor<R>(typeOf<R>())
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified R> Context.createEditor() = createEditor<R>(typeOf<R>())

/**
 * Uses the [EditorFactory] of this [Context] to create an [Editor] with the result type [R], having the initial specified [result].
 * If this [Context] has no [EditorFactory] or there isn't an editor registered in the [EditorFactory],
 * this method recursively tries to create an [Editor] with the [Context.parent] [Context]
 * @throws NoSuchElementException if neither this context nor any of its parent contexts can create an [Editor]
 * for result type [R]
 */
fun <R> Context.createEditor(type: KType, result: R): Editor<R> =
    try {
        get(EditorFactory).createEditor(type, result, this)
    } catch (e: NoSuchElementException) {
        parent?.createEditor(type, result) ?: throw e
    }

/**
 * Syntactic sugar for createEditor(typeOf<R>(), result)
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified R> Context.createEditor(result: R) = createEditor(
    typeOf<R>(), result
)

/**
 * Create a new context which has this [Context] as its parent and apply the given [block] to it.
 */
inline fun Context.extend(block: Context.() -> Unit): Context =
    Context.newInstance(this, block)

/**
 * Create an [Output] to the given [path] with the [SerialProperties.serialContext] and [SerialProperties.serial] of this [Context].
 */
fun Context.createOutput(path: Path): Output {
    val ctx = get(SerialProperties.serialContext)
    val serial = get(SerialProperties.serial)
    return serial.createOutput(path, ctx)
}

/**
 * Create an [Input] from the given [path] with the [SerialProperties.serialContext] and [SerialProperties.serial] of this [Context].
 */
fun Context.createInput(path: Path): Input {
    val ctx = get(SerialProperties.serialContext)
    val serial = get(SerialProperties.serial)
    return serial.createInput(path, ctx)
}