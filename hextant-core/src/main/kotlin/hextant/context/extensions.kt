package hextant.context

import bundles.*
import hextant.core.Editor
import hextant.core.editor.BidirectionalEditor
import hextant.core.view.EditorControl
import hextant.generated.createEditor
import hextant.plugin.Aspects
import hextant.serial.SerialProperties
import kserial.*
import java.nio.file.Path
import java.util.logging.Level
import kotlin.reflect.KClass

/**
 * Set the given [property] to the given [value] on the first [Context] in the parent-chain that already has this property.
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
fun Context.createControl(editor: Editor<*>, arguments: Bundle = createBundle()): EditorControl<*> {
    val group = get(EditorControlGroup)
    return group.createViewFor(editor, this, arguments)
}

/**
 * Create a view for the given [editor]. The [configure]-block is used to initialize the [hextant.core.EditorView.arguments].
 */
inline fun Context.createControl(editor: Editor<*>, configure: Bundle.() -> Unit): EditorControl<*> =
    createControl(editor, createBundle(configure))

/**
 * Uses the [EditorFactory] aspect of this [Context] to create an [Editor] with the given [resultType].
 * @throws NoSuchElementException if there is no editor registered for the given result type.
 */
fun <R : Any> Context.createEditor(resultType: KClass<R>): Editor<R> =
    get(Aspects).createEditor(resultType, this)

/**
 * Uses the [EditorFactory] aspect of this [Context] to create an [Editor] with the given result type
 * and then tries to cast it to a [BidirectionalEditor] to set the result to the given value.
 * @throws NoSuchElementException if there is no editor registered for the given result type
 * or the registered editor is not a [BidirectionalEditor].
 */
fun <R : Any> Context.createEditor(result: R): Editor<R> {
    val e = createEditor(result::class)
    if (e !is BidirectionalEditor) throw NoSuchElementException("Cannot create bidirectional editor for ${result::class}")
    e.setResult(result)
    return e
}

/**
 * Syntactic sugar for createEditor<R>(typeOf<R>())
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified R : Any> Context.createEditor() = createEditor<R>(R::class)

/**
 * Create a new context which has this [Context] as its parent and apply the given [block] to it.
 */
inline fun Context.extend(block: Context.() -> Unit = {}): Context =
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

/**
 * Tries to execute the given action catching an eventual thrown exception and logging it.
 */
inline fun <T> Context.executeSafely(description: String, onError: T, action: () -> T): T = try {
    action()
} catch (ex: Throwable) {
    val msg = "Exception while $description: ${ex.message}"
    get(HextantPlatform.logger).log(Level.SEVERE, msg, ex)
    onError
}