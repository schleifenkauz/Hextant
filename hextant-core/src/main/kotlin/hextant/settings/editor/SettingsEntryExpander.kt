/**
 *@author Nikolaus Knop
 */

package hextant.settings.editor

import hextant.codegen.ProvideFeature
import hextant.context.Context
import hextant.context.Internal
import hextant.core.editor.Expander
import hextant.settings.model.*

@ProvideFeature
internal class SettingsEntryExpander(context: Context) : Expander<SettingsEntry, SettingsEntryEditor>(context) {
    override fun expand(text: String): SettingsEntryEditor? {
        val properties = context[Internal, ConfigurableProperties]
        val property = properties.byName(text) ?: return null
        if (context[Internal, Settings].hasProperty(property.property)) return null
        return SettingsEntryEditor(context, property)
    }
}