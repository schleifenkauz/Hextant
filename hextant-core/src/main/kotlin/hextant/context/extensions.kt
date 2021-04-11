package hextant.context

import bundles.*
import hextant.core.Editor
import hextant.core.view.EditorControl
import hextant.generated.createEditor
import hextant.plugins.Aspects
import hextant.undo.UndoManager
import hextant.undo.withoutUndo
import java.util.logging.Level
import kotlin.reflect.KClass

/**
 * Set the given [property] to the given [value] on the first [Context] in the parent-chain that already has this property.
 */
fun <T : Any, P : Permission> Context.replace(permission: P, property: Property<T, P>, value: T) {
    when {
        hasProperty(property) -> set(permission, property, value)
        parent != null        -> parent!!.replace(permission, property, value)
        else                  -> throw NoSuchElementException("Property $property not configured")
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
inline fun Context.createControl(editor: Editor<*>, configure: BundleBuilder.() -> Unit): EditorControl<*> =
    createControl(editor, createBundle(configure))

/**
 * Uses the [EditorFactory] aspect of this [Context] to create an [Editor] with the given [resultType].
 * @throws NoSuchElementException if there is no editor registered for the given result type.
 */
fun <R : Any> Context.createEditor(resultType: KClass<R>): Editor<R?> =
    get(Aspects).createEditor(resultType, this)

/**
 * Syntactic sugar for createEditor<R>(R::class)
 */
inline fun <reified R : Any> Context.createEditor() = createEditor(R::class)

/**
 * Create a new context which has this [Context] as its parent and apply the given [block] to it.
 */
inline fun Context.extend(block: Context.() -> Unit = {}): Context =
    Context.create(this, block)

/**
 * Tries to execute the given action catching an eventual thrown exception and logging it.
 */
inline fun <T> Context.executeSafely(description: String, onError: T, action: () -> T): T = try {
    action()
} catch (ex: Throwable) {
    val msg = "Exception while $description: ${ex.message}"
    get(Properties.logger).log(Level.SEVERE, msg, ex)
    onError
}

/**
 * Deactivates the [UndoManager] of this context while executing the given [action] and then reactivates it.
 */
inline fun <T> Context.withoutUndo(action: () -> T): T = get(UndoManager).withoutUndo(action)

/**
 * Executes the given [action] on this [Editor] without recording undoable edits and then returns the receiver.
 * @see Context.withoutUndo
 */
inline fun <E : Editor<*>> E.withoutUndo(action: E.() -> Unit): E {
    context.withoutUndo { action() }
    return this
}