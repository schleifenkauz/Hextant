/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Clipboard
import hextant.context.ClipboardContent.OneEditor
import hextant.context.Context
import hextant.context.executeSafely
import hextant.core.Editor

/**
 * Return an [ExpanderDelegate] that transforms expanded editors with the given function.
 */
fun <E : Editor<*>, R : Editor<*>> ExpanderDelegate<E>.map(f: (E) -> R) = object : ExpanderDelegate<R> {
    override fun expand(text: String, context: Context): R? = this@map.expand(text, context)?.let(f)

    override fun expand(item: Any, context: Context): R? = this@map.expand(item, context)?.let(f)
}

/**
 * Return a [TokenType] that transforms compiled results with the given function.
 */
fun <R, F> TokenType<R>.map(f: (R) -> F) = TokenType { token -> f(this@map.compile(token)) }

/**
 * Return a sequence iterating over all immediate and recursive children of this editor
 */
val Editor<*>.allChildren: Sequence<Editor<*>>
    get() {
        val directChildren = getChildren().asSequence()
        return directChildren + directChildren.flatMap { e -> e.allChildren }
    }


@Suppress("UNCHECKED_CAST")
fun <E: Editor<*>> E.snapshot() = implCopy() as E

/**
 * Copy this editor for the given [newContext]
 */
fun <E : Editor<*>> E.copyFor(newContext: Context): E = snapshot().also { e -> e.initialize(newContext) }

/**
 * Copy this [Editor] to the [Clipboard], if this is supported by the editor.
 * Returns `true` only if the action was successful.
 */
fun Editor<*>.copyToClipboard(): Boolean {
    if (!supportsCopyPaste()) return false
    val snapshot = snapshot()
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
    return context.executeSafely("pasting", false) { paste(content.content) }
}

/**
 * Return an editor that transforms the [Editor.result] of this editor with the given function.
 */
inline fun <T, R> Editor<T>.map(crossinline f: (T) -> R): Editor<R> = object : TransformedEditor<T, R>(this@map) {
    override fun transform(result: T): R = f(result)
}

inline fun <reified P : Editor<*>> Editor<*>.getParent(): P? {
    var e = this
    while (true) {
        if (e is P) return e
        e = e.parent ?: return null
    }
}

fun <E: Editor<*>> E.initialized(context: Context) = also { e -> e.initialize(context) }

fun <R> Editor<*>.makeUndoableEdit(description: String, edit: () -> R): R {
    //TODO
    return edit()
}

fun <E: Editor<*>> E.defaultState() = also { e -> e.setupDefaultState() }