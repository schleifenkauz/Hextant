/**
 *@author Nikolaus Knop
 */

package hextant.config

import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
internal class FeatureIdEditor(val enabled: Boolean) :
    TokenEditor<@Contextual Feature?, TokenEditorView>() {
    override fun compile(token: String): Feature? = context[FeatureRegistrar].getFeature(token)
}