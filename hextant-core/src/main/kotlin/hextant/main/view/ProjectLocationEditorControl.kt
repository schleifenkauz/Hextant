/**
 *@author Nikolaus Knop
 */

package hextant.main.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.TokenEditorControl
import hextant.main.editor.ProjectLocationEditor

internal class ProjectLocationEditorControl @ProvideImplementation(
    ControlFactory::class,
    ProjectLocationEditor::class
) constructor(editor: ProjectLocationEditor, args: Bundle) :
    TokenEditorControl(editor, args, ProjectLocationCompleter)