/**
 *@author Nikolaus Knop
 */

package hextant.settings.view

import bundles.Bundle
import hextant.core.view.CompoundEditorControl
import hextant.settings.editor.SettingsEntryEditor

internal class SettingsEntryEditorControl constructor(editor: SettingsEntryEditor, args: Bundle) :
    CompoundEditorControl(editor, args, {
        line {
            keyword(editor.property.property.name)
            space()
            operator("=")
            space()
            view(editor.value)
        }
    })