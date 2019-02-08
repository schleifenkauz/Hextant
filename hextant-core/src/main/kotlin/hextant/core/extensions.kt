package hextant.core

import hextant.*
import hextant.base.EditorControl

inline fun <reified E : Editable<*>, reified Ed : Editor<E>> EditorFactory.register(
    noinline factory: (E, Context) -> Ed
) {
    register(E::class, factory)
}

inline fun <reified E : Editable<*>> EditorControlFactory.register(
    noinline viewFactory: (E, Context) -> EditorControl<*>
) {
    register(E::class, viewFactory)
}

inline fun EditorControlFactory.configure(config: EditorControlFactory.() -> Unit) {
    apply(config)
}

/**
 * Syntactic sugar for register(T::class, factory)
 */
inline fun <reified T : Any> EditableFactory.register(noinline factory: (T) -> Editable<T>) {
    register(T::class, factory)
}

/**
 * Syntactic sugar for register(T::class, factory)
 */
inline fun <reified T : Any> EditableFactory.register(noinline factory: () -> Editable<T>) {
    register(T::class, factory)
}