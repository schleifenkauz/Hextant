/**
 *@author Nikolaus Knop
 */

package hextant.config

import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView

internal class FeatureIdEditor(val enabled: Boolean) :
    TokenEditor<Feature?, TokenEditorView>() {
    override fun compile(token: String): Feature? = context[FeatureRegistrar].getFeature(token)
}