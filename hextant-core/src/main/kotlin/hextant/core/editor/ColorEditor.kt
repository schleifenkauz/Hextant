/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.codegen.ProvideFeature
import hextant.context.Context
import hextant.core.view.TokenEditorView
import javafx.scene.paint.Color

/**
 * Editor for colors based on the [Color.web] function.
 */
@ProvideFeature
class ColorEditor(context: Context) : TokenEditor<String?, TokenEditorView>(context) {
    override fun compile(token: String): String? = try {
        Color.web(token)
        token
    } catch (ex: IllegalArgumentException) {
        null
    }
}