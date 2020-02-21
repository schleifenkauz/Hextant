/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Context
import hextant.Editor

/**
 * Return an [ExpanderDelegate] that transforms expanded editors with the given function.
 */
fun <E : Editor<*>, R : Editor<*>> ExpanderDelegate<E>.map(f: (E) -> R) = object : ExpanderDelegate<R> {
    override fun expand(text: String, context: Context): R? = this@map.expand(text, context)?.let { f(it) }
}