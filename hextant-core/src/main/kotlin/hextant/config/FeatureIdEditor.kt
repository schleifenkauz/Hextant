/**
 *@author Nikolaus Knop
 */

package hextant.config

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView

internal class FeatureIdEditor(context: Context, val enabled: Boolean) :
    TokenEditor<Feature?, TokenEditorView>(context) {
    override fun compile(token: String): Feature? = context[FeatureRegistrar].getFeature(token)
}