/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.core.Editor

/**
 * [ConfiguredExpander] delegates invocations of [expand] to objects implementing this interface
 */
interface ExpanderDelegate<out E : Editor<*>> {
    /**
     * Expand the given [text] using the specified [context].
     */
    fun expand(text: String, expander: Expander<*, *>): E?

    /**
     * Expand the given completion-[item] using the specified [context].
     */
    fun expand(item: Any, expander: Expander<*, *>): E?
}