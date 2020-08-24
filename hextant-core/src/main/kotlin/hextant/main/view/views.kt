/**
 * @author Nikolaus Knop
 */

package hextant.main.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.TokenEditorControl
import hextant.main.editor.ProjectLocationEditor
import hextant.main.editor.ProjectTypeEditor

@ProvideImplementation(ControlFactory::class)
internal fun createControl(editor: ProjectTypeEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, ProjectTypeCompleter)

@ProvideImplementation(ControlFactory::class)
internal fun createControl(editor: ProjectLocationEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, ProjectLocationCompleter)