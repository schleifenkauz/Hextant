package hextant

inline fun EditorControlFactory.configure(config: EditorControlFactory.() -> Unit) {
    apply(config)
}

/**
 * Syntactic sugar for `register(T::class, factory)`
 */
inline fun <reified T : Any> EditorFactory.register(noinline factory: (T, Context) -> Editor<T>) {
    register(T::class, factory)
}

/**
 * Syntactic sugar for `register(T::class, factory)`
 */
inline fun <reified T : Any> EditorFactory.register(noinline factory: (Context) -> Editor<T>) {
    register(T::class, factory)
}