package hextant.core

import hextant.context.*
import hextant.context.ClipboardContent.OneEditor
import hextant.core.editor.TransformedEditor
import validated.Validated
import validated.valid

/**
 * Return a sequence iterating over all immediate and recursive children of this editor
 */
val Editor<*>.allChildren: Sequence<Editor<*>>
    get() {
        val directChildren = children.now.asSequence()
        return directChildren.asSequence() + directChildren.flatMap { it.allChildren }
    }

/**
 * If this editor is already in the specified [newContext] just returns it, otherwise copies the editor to the new context
 */
fun <E : Editor<*>> E.moveTo(newContext: Context): E =
    if (this.context == newContext) this else this.copyFor(newContext)

/**
 * Copy this editor for the given [newContext]
 */
fun <E : Editor<*>> E.copyFor(newContext: Context): E = snapshot().reconstruct(newContext)

/**
 * Copy this [Editor] to the [Clipboard], if this is supported by the editor.
 * Returns `true` only if the action was successful.
 */
fun Editor<*>.copyToClipboard(): Boolean {
    if (!supportsCopyPaste()) return false
    context[Clipboard].copy(OneEditor(snapshot()))
    return true
}

/**
 * Paste the [Clipboard]-content into this editor.
 * Returns `true` only if the action was successful.
 */
fun Editor<*>.pasteFromClipboard(): Boolean {
    val content = context[Clipboard].get()
    if (content !is OneEditor) return false
    return paste(content.snapshot)
}

/**
 * Returns a copy of the given Editor for the same [Context]
 */
inline fun <reified E : Editor<*>> E.copy(): E = copyFor(context)

/**
 * Return an editor that transforms the [Editor.result] of this editor with the given function.
 */
fun <T, R> Editor<T>.map(f: (T) -> Validated<R>): Editor<R> = TransformedEditor(this, f)

/**
 * Return an editor that transforms the [Editor.result] of this editor with the given function.
 */
@JvmName("simpleMap")
fun <T, R> Editor<T>.map(f: (T) -> R): Editor<R> = map { valid(f(it)) }

