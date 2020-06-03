/**
 *@author Nikolaus Knop
 */

package hextant.settings.editors

import bundles.Property
import hextant.Context
import hextant.core.Internal
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.settings.model.ConfigurableProperties
import validated.*

internal class PropertyEditor(context: Context) : TokenEditor<Property<*, *, *>, TokenEditorView>(context) {
    private val properties = context[Internal, ConfigurableProperties]

    override fun compile(token: String): Validated<Property<*, *, *>> =
        properties.byName(token)?.property.validated { invalid("No property with name '$token") }
}