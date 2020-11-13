/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor

/**
 * [ConfiguredExpander] delegates invocations of [expand] to objects implementing this interface
 */
interface ExpanderDelegate<out E : Editor<*>> {
    /**
     * Expand the given [text] using the specified [context].
     */
    fun expand(text: String, context: Context): E?

    /**
     * Expand the given completion-[item] using the specified [context].
     */
    fun expand(item: Any, context: Context): E?
}