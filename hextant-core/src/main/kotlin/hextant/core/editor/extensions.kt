/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Context
import hextant.Editor

fun <E : Editor<*>, R : Editor<*>> ExpanderDelegate<E>.map(f: (E) -> R) = object : ExpanderDelegate<R> {
    override fun expand(text: String, context: Context): R? = this@map.expand(text, context)?.let { f(it) }
}