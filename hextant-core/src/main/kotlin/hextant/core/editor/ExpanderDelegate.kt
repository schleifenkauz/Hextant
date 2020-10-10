/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.core.Editor

/**
 * [ConfiguredExpander] delegates invocations of [expand] to objects implementing this interface
 */
interface ExpanderDelegate<out E : Editor<*>, in Ctx> {
    /**
     * Expand the given [text] using the specified [context].
     */
    fun expand(text: String, context: Ctx): E?

    /**
     * Expand the given completion[item] using the specified [context].
     */
    fun expand(item: Any, context: Ctx): E?
}