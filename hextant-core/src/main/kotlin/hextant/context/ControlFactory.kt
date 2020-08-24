/**
 * @author Nikolaus Knop
 */

package hextant.context

import bundles.Bundle
import hextant.codegen.RequestAspect
import hextant.core.Editor
import hextant.core.view.EditorControl

/**
 * Interface that allows the creation of views for editors.
 */
@RequestAspect
interface ControlFactory<E : Editor<*>> {
    /**
     * Create a new [EditorControl] for the given [editor].
     */
    fun createControl(editor: E, arguments: Bundle): EditorControl<*>
}