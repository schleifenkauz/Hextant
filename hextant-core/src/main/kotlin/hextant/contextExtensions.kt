/**
 * @author Nikolaus Knop
 */

package hextant

import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.serial.SerialProperties
import kserial.*
import java.nio.file.Path
import kotlin.reflect.KClass

/**
 * Uses the [EditorControlGroup] of this [Context] to create a view for the given [editor] with the specified [arguments]
 */
fun Context.createView(editor: Editor<*>, arguments: Bundle = Bundle.newInstance()): EditorControl<*> {
    val group = get(EditorControlGroup)
    return group.createViewFor(editor, this, arguments)
}

fun Context.createView(editor: Editor<*>, configure: Bundle.() -> Unit): EditorControl<*> =
    createView(editor, Bundle.configure(configure))

/**
 * Uses the [EditorFactory] of this [Context] to create an [Editor] with the result type [R].
 * If this [Context] has no [EditorFactory] or there isn't an editor registered in the [EditorFactory],
 * this method recursively tries to create an [Editor] with the [Context.parent] [Context].
 * This method is invariant in the sense that it could also create an [Editor] for results of type `S` if `S` is a subtype of `R`.
 * @throws NoSuchElementException if neither this context nor any of its parent contexts can create an [Editor]
 * for result type [R]
 */
fun <R : Any> Context.createEditor(resultCls: KClass<R>): Editor<R> =
    try {
        get(Public, EditorFactory).getEditor(resultCls, this)
    } catch (e: NoSuchElementException) {
        parent?.createEditor(resultCls) ?: throw e
    }

/**
 * Uses the [EditorFactory] of this [Context] to create an [Editor] with the result type [R], having the initial specified [result].
 * If this [Context] has no [EditorFactory] or there isn't an editor registered in the [EditorFactory],
 * this method recursively tries to create an [Editor] with the [Context.parent] [Context]
 * @throws NoSuchElementException if neither this context nor any of its parent contexts can create an [Editor]
 * for result type [R]
 */
fun <R : Any> Context.createEditor(result: R): Editor<R> =
    try {
        get(Public, EditorFactory).getEditor(result, this)
    } catch (e: NoSuchElementException) {
        parent?.createEditor(result) ?: throw e
    }

/**
 * Syntactic sugar for `get(Public, property)`
 */
@JvmName("getPublic")
operator fun <T : Any> Context.get(property: Property<T, Public, *>): T = get(Public, property)

/**
 * Syntactic sugar for `set(Public, property, value)`
 */
@JvmName("setPublic")
operator fun <T : Any> Context.set(property: Property<T, *, Public>, value: T) =
    set(Public, property, value)

@JvmName("getInternal")
internal operator fun <T : Any> Context.get(property: Property<T, Internal, *>): T = get(
    Internal, property
)

@JvmName("setInternal")
internal operator fun <T : Any> Context.set(property: Property<T, *, Internal>, value: T) =
    set(Internal, property, value)

/**
 * Delegates to [HextantPlatform.runLater] of the [Context.platform] of this [Context]
 */
fun Context.runLater(action: () -> Unit) = platform.runLater(action)

/**
 * Create a new context which has this [Context] as its parent and apply the given [block] to it.
 */
inline fun Context.extend(block: Context.() -> Unit): Context = Context.newInstance(this, block)

fun Context.createOutput(path: Path): Output {
    val ctx = get(SerialProperties.serialContext)
    val serial = get(SerialProperties.serial)
    return serial.createOutput(path, ctx)
}

fun Context.createInput(path: Path): Input {
    val ctx = get(SerialProperties.serialContext)
    val serial = get(SerialProperties.serial)
    return serial.createInput(path, ctx)
}