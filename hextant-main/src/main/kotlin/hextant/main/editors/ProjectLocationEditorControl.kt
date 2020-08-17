/**
 *@author Nikolaus Knop
 */

package hextant.main.editors

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.core.view.TokenEditorControl
import hextant.fx.registerShortcuts
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter

@ProvideImplementation
class ProjectLocationEditorControl(editor: ProjectLocationEditor, args: Bundle) : TokenEditorControl(editor, args) {
    init {
        registerShortcuts {
            on("Ctrl+E") {
                val f = fc.showOpenDialog(null)
                if (f != null) editor.setText(f.absolutePath)
            }
        }
    }

    companion object {
        private val fc = FileChooser()

        init {
            fc.extensionFilters.add(ExtensionFilter("Hextant Projects", "*.hxt"))
        }
    }
}