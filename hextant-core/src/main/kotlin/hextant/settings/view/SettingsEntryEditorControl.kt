/**
 *@author Nikolaus Knop
 */

package hextant.settings.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl
import hextant.settings.editor.SettingsEntryEditor

internal class SettingsEntryEditorControl @ProvideImplementation(
    ControlFactory::class,
    SettingsEntryEditor::class
) constructor(editor: SettingsEntryEditor, args: Bundle) :
    CompoundEditorControl(editor, args, {
        line {
            keyword(editor.property.property.name)
            space()
            operator("=")
            space()
            view(editor.value)
        }
    })