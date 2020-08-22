/**
 * @author Nikolaus Knop
 */

package hextant.context

import hextant.codegen.RequestAspect
import hextant.core.Editor

/**
 * Aspect that allows the creation of editors for specific result types.
 */
@RequestAspect
interface EditorFactory<R : Any> {
    /**
     * Creates a new editor for result of type [R] using the given [context].
     */
    fun createEditor(context: Context): Editor<R>
}