/**
 *@author Nikolaus Knop
 */

package hextant.settings.editors

import hextant.Context
import hextant.core.editor.ListEditor
import hextant.settings.model.SettingsEntry

class SettingsEntryListEditor(context: Context) : ListEditor<SettingsEntry, SettingsEntryExpander>(context) {
    override fun createEditor(): SettingsEntryExpander =
        SettingsEntryExpander(context)
}