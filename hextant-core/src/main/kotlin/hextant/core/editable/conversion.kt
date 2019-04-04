/**
 * @author Nikolaus Knop
 */

package hextant.core.editable

import hextant.*
import reaktive.value.binding.map

@PublishedApi internal class ConvertedEditable<T, F : Any>(
    internal val source: Editable<T>, f: (T) -> CompileResult<F>
) : Editable<F> {
    override val result: RResult<F> =
        source.result.map { r -> r.flatMap(f) }
}

/**
 * @return an [Editable] that is ok if the receiver is ok and maps the results with [f]
 */
inline fun <reified F : Any, T> Editable<T>.flatMap(noinline f: (T) -> CompileResult<F>): Editable<F> =
    ConvertedEditable(this, f)

inline fun <reified F : Any, T : Any> Editable<T>.map(noinline f: (T) -> F): Editable<F> =
    flatMap { Ok(f(it)) }


