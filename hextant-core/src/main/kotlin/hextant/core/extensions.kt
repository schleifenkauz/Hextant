package hextant.core

import hextant.*
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.core.editable.Expandable

inline fun <reified E : Editable<*>, reified Ed : Editor<E>> EditorFactory.register(
    noinline factory: (E, Context) -> Ed
) {
    register(E::class, factory)
}

inline fun <reified E : Editable<*>> EditorControlFactory.register(
    noinline viewFactory: (E, Context, Bundle) -> EditorControl<*>
) {
    register(E::class, viewFactory)
}

inline fun EditorControlFactory.configure(config: EditorControlFactory.() -> Unit) {
    apply(config)
}

/**
 * Syntactic sugar for `register(T::class, factory)`
 */
inline fun <reified T : Any> EditableFactory.register(noinline factory: (T) -> Editable<T>) {
    register(T::class, factory)
}

/**
 * Syntactic sugar for `register(T::class, factory)`
 */
inline fun <reified T : Any> EditableFactory.register(noinline factory: () -> Editable<T>) {
    register(T::class, factory)
}

/**
 * Syntactic sugar for `register(E::class, factory)`
 */
inline fun <reified E : Editable<*>> ExpandableFactory.register(noinline factory: () -> Expandable<*, E>) {
    register(E::class, factory)
}

/**
 * Syntactic sugar for `createExpandable(E::class) as Ex`
 */
inline fun <reified E : Editable<*>, reified Ex : Expandable<*, E>> ExpandableFactory.createExpandable(): Ex =
    createExpandable(E::class) as Ex

/**
 * Returns an expandable created by this [ExpandableFactory] wrapping the given [editable]
 * * Note the distinction between the class of the type parameter [E] and the class of the given [editable]
 * * An Expandable is created for the class [E] and not for the possible subclass of [E] that [editable] has
 */
inline fun <reified E : Editable<*>, reified Ex : Expandable<*, E>> ExpandableFactory.wrapInExpandable(editable: E): Expandable<*, E> {
    val expandable = createExpandable<E, Ex>()
    expandable.setContent(editable)
    return expandable
}