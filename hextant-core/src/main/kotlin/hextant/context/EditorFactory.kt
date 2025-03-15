/**
 * @author Nikolaus Knop
 */

package hextant.context

import hextant.codegen.RequestAspect
import hextant.core.Editor

/**
 * Aspect that allows the creation of editors for specific result types.
 */
@RequestAspect(optional = true)
fun interface EditorFactory<out R : Any> {
    /**
     * Creates a new editor for results of type [R].
     */
    fun createEditor(): Editor<R?>
}