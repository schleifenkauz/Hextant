/**
 * @author Nikolaus Knop
 */

package hextant.base

import hextant.Editor
import hextant.base.CompoundEditorControl.Compound
import hextant.bundle.Bundle

fun Compound.view(editor: Editor<*>, bundle: Bundle = Bundle.newInstance(), config: Bundle.() -> Unit) {
    view(editor, bundle.apply(config))
}