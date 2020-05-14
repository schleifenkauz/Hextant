/**
 * @author Nikolaus Knop
 */

package hextant.fx

import hextant.Context
import hextant.core.Internal
import javafx.scene.Scene

/**
 * Initializes this scene with the given [context] by registering top level shortcuts and applying registered stylesheets.
 */
fun Scene.initHextantScene(context: Context) {
    registerNavigationShortcuts()
    registerCopyPasteShortcuts(context)
    context[Internal, Stylesheets].apply(this)
}

internal val Scene.focusedEditorControl: EditorControl<*>?
    get() = editorControlInParentChain(focusOwner)
