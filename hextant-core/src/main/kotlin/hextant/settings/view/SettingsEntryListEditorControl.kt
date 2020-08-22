/**
 *@author Nikolaus Knop
 */

package hextant.settings.view

import bundles.Bundle
import hextant.core.view.ListEditorControl
import hextant.settings.editor.SettingsEntryListEditor

internal class SettingsEntryListEditorControl(editor: SettingsEntryListEditor, args: Bundle) :
    ListEditorControl(editor, args, Orientation.Vertical)