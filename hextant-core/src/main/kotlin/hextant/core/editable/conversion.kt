/**
 * @author Nikolaus Knop
 */

package hextant.core.editable

import hextant.Editable
import reaktive.value.ReactiveBoolean
import reaktive.value.ReactiveValue
import reaktive.value.binding.map

@PublishedApi internal class ConvertedEditable<T, F : Any>(
    internal val source: Editable<T>, f: (T?) -> F?
) : Editable<F> {
    override val edited: ReactiveValue<F?> =
        source.edited.map(f)
    override val isOk: ReactiveBoolean
        get() = source.isOk
}

/**
 * @return an [Editable] that is ok if the receiver is ok and maps the edited objects with [f]
 */
inline fun <reified F : Any, T> Editable<T>.map(noinline f: (T?) -> F?): Editable<F> =
    ConvertedEditable(this, f)

inline fun <reified F : Any, T : Any> Editable<T>.mapNotNull(noinline f: (T) -> F): Editable<F> =
    map { it?.let(f) }


