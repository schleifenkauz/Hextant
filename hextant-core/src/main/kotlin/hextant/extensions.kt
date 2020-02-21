package hextant

import hextant.bundle.CoreProperties.clipboard
import hextant.bundle.Internal
import hextant.core.editor.TransformedEditor
import hextant.core.editor.getSimpleEditorConstructor

/**
 * Synonym for [apply]
 */
inline fun EditorControlFactory.configure(config: EditorControlFactory.() -> Unit) {
    apply(config)
}

/**
 * Syntactic sugar for `register(T::class, factory)`
 */
inline fun <reified T : Any> EditorFactory.register(noinline factory: (Context, T) -> Editor<T>) {
    register(T::class, factory)
}

/**
 * Syntactic sugar for `register(T::class, factory)`
 */
inline fun <reified T : Any> EditorFactory.register(noinline factory: (Context) -> Editor<T>) {
    register(T::class, factory)
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
fun <E : Editor<*>> E.copyFor(newContext: Context): E {
    val cls = this::class
    val cstr = cls.getSimpleEditorConstructor()
    val new = cstr(newContext)
    val supported = new.paste(this)
    check(supported) { "Copy is not supported" }
    return new
}

/**
 * Copy this [Editor] to the [clipboard], if this is supported by the editor.
 * Returns `true` only if the action was successful.
 */
fun Editor<*>.copyToClipboard(): Boolean {
    if (!supportsCopyPaste()) return false
    context[Internal, clipboard] = this
    return true
}

/**
 * Paste the [clipboard]-content into this editor.
 * Returns `true` only if the action was successful.
 */
fun Editor<*>.pasteFromClipboard(): Boolean {
    val content = context[clipboard]
    if (content !is Editor<*>) return false
    return paste(content)
}

/**
 * Returns a copy of the given Editor for the same [Context]
 */
inline fun <reified E : Editor<*>> E.copy(): E = copyFor(context)

/**
 * Return an editor that transforms the [Editor.result] of this editor with the given function.
 */
fun <T : Any, R : Any> Editor<T>.map(f: (T) -> CompileResult<R>): Editor<R> = TransformedEditor(this, f)

/**
 * Return an editor that transforms the [Editor.result] of this editor with the given function.
 */
@JvmName("simpleMap")
fun <T : Any, R : Any> Editor<T>.map(f: (T) -> R): Editor<R> = map { ok(f(it)) }