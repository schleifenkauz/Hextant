/**
 * @author Nikolaus Knop
 */

package hextant.base

import hextant.Editor
import hextant.base.CompoundEditorControl.Compound
import hextant.bundle.Bundle

/**
 * Add the editor control for the given [editor] to this compound view.
 * The [config] block is used to initialize properties of the [hextant.EditorView.arguments] bundle.
 */
fun Compound.view(editor: Editor<*>, bundle: Bundle = Bundle.newInstance(), config: Bundle.() -> Unit) {
    view(editor, bundle.apply(config))
}