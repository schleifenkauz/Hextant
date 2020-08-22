/**
 *@author Nikolaus Knop
 */

package hextant.settings.editor

import hextant.codegen.ProvideFeature
import hextant.context.Context
import hextant.core.editor.ListEditor
import hextant.settings.model.SettingsEntry

@ProvideFeature
internal class SettingsEntryListEditor(context: Context) : ListEditor<SettingsEntry, SettingsEntryExpander>(context) {
    override fun createEditor(): SettingsEntryExpander =
        SettingsEntryExpander(context)
}