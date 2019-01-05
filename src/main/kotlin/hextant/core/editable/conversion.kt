/**
 * @author Nikolaus Knop
 */

package hextant.core.editable

import hextant.Editable
import org.nikok.reaktive.value.ReactiveBoolean
import org.nikok.reaktive.value.ReactiveValue
import kotlin.reflect.KClass

@PublishedApi internal class ConvertedEditable<T, F : Any>(
    fCls: KClass<F>, internal val source: Editable<T>, f: (T?) -> F?
) : Editable<F> {
    override val edited: ReactiveValue<F?> =
        source.edited.map("${source.edited.description} converted to ${fCls.simpleName}", f)
    override val isOk: ReactiveBoolean
        get() = source.isOk
}

/**
 * @return an [Editable] that is ok if the receiver is ok and maps the edited objects with [f]
 */
inline fun <reified F : Any, T> Editable<T>.map(noinline f: (T?) -> F?): Editable<F> =
    ConvertedEditable(F::class, this, f)

inline fun <reified F : Any, T : Any> Editable<T>.mapNotNull(noinline f: (T) -> F): Editable<F> =
    map { it?.let(f) }


