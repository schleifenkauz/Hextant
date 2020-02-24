/**
 *@author Nikolaus Knop
 */

package hextant.settings.editors

import hextant.*
import hextant.bundle.Internal
import hextant.bundle.Property
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.settings.model.ConfigurableProperties

internal class PropertyEditor(context: Context) : TokenEditor<Property<*, *, *>, TokenEditorView>(context) {
    private val properties = context[Internal, ConfigurableProperties]

    override fun compile(token: String): CompileResult<Property<*, *, *>> =
        properties.byName(token)?.property.okOrErr { "No property with name '$token" }
}