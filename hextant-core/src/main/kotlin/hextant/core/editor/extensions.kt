/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.*
import hextant.context.ClipboardContent.OneEditor
import hextant.core.Editor
import hextant.serial.reconstructEditor
import hextant.serial.snapshot

/**
 * Return an [ExpanderDelegate] that transforms expanded editors with the given function.
 */
fun <E : Editor<*>, R : Editor<*>> ExpanderDelegate<E>.map(f: (E) -> R) = object : ExpanderDelegate<R> {
    override fun expand(text: String, context: Context): R? = this@map.expand(text, context)?.let(f)

    override fun expand(item: Any, context: Context): R? = this@map.expand(item, context)?.let(f)
}

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
fun <E : Editor<*>> E.copyFor(newContext: Context): E = snapshot(recordClass = true).reconstructEditor(newContext)

/**
 * Copy this [Editor] to the [Clipboard], if this is supported by the editor.
 * Returns `true` only if the action was successful.
 */
fun Editor<*>.copyToClipboard(): Boolean {
    if (!supportsCopyPaste()) return false
    val snapshot = snapshot(recordClass = true)
    context[Clipboard].copy(OneEditor(snapshot))
    return true
}

/**
 * Replaces this editor by the [other] one by setting the editor of its [Editor.expander] to the [other] editor.
 */
@Suppress("UNCHECKED_CAST")
fun <E : Editor<*>> E.replaceWith(other: E) {
    val ex = expander as Expander<*, E>
    ex.expand(other)
}

/**
 * Paste the [Clipboard]-content into this editor.
 * Returns `true` only if the action was successful.
 */
fun Editor<*>.pasteFromClipboard(): Boolean {
    val content = context[Clipboard].get()
    if (content !is OneEditor) return false
    return context.executeSafely("pasting", false) { paste(content.snapshot) }
}

/**
 * Returns a copy of the given Editor for the same [Context]
 */
inline fun <reified E : Editor<*>> E.copy(): E = copyFor(context)

/**
 * Return an editor that transforms the [Editor.result] of this editor with the given function.
 */
fun <T, R> Editor<T>.map(f: (T) -> R): Editor<R> = TransformedEditor(this, f)