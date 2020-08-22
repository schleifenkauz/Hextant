/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import bundles.Bundle
import hextant.core.editor.ValidatedTokenEditor
import hextant.core.view.ValidatedTokenEditorControl
import hextant.fx.shortcut
import javafx.scene.input.KeyCode.*

internal class FileNameEditorControl(editor: ValidatedTokenEditor<*>, arguments: Bundle) :
    ValidatedTokenEditorControl(editor, arguments.apply {
        set(BEGIN_CHANGE, shortcut(F2))
        set(ABORT_CHANGE, shortcut(ESCAPE))
        set(COMMIT_CHANGE, shortcut(ENTER))
    })