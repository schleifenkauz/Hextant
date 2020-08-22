/**
 *@author Nikolaus Knop
 */

package hextant.settings.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl
import hextant.settings.editor.SettingsEditor

internal class SettingsEditorControl @ProvideImplementation(ControlFactory::class, SettingsEditor::class) constructor(
    editor: SettingsEditor, args: Bundle
) : CompoundEditorControl(editor, args, {
    view(editor.entries)
})