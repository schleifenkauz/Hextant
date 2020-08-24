/**
 *@author Nikolaus Knop
 */

package hextant.settings.view

import bundles.Bundle
import hextant.core.view.CompoundEditorControl
import hextant.settings.editor.SettingsEditor

internal class SettingsEditorControl constructor(
    editor: SettingsEditor, args: Bundle
) : CompoundEditorControl(editor, args, {
    view(editor.entries)
})