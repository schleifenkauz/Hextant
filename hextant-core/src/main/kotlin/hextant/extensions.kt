package hextant

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
inline fun <reified E : Editor<*>> E.moveTo(newContext: Context): E =
    if (this.context == newContext) this else this.copyForImpl(newContext) as E

/**
 * Type safe version of [Editor.copyForImpl] casts the editor returned by [Editor.copyForImpl] to [E]
 */
inline fun <reified E : Editor<*>> E.copyFor(newContext: Context): E = copyForImpl(newContext) as E

/**
 * Returns a copy of the given Editor for the same [Context]
 */
inline fun <reified E : Editor<*>> E.copy(): E = copyFor(context)