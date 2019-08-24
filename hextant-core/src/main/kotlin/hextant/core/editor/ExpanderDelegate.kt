/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Context
import hextant.Editor

/**
 * [ConfiguredExpander] delegates invocations of [expand] to objects implementing this interface
*/
interface ExpanderDelegate<out E: Editor<*>> {
    fun expand(text: String, context: Context): E?
}