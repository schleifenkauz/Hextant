/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.codegen.ProvideFeature
import hextant.context.Context
import hextant.core.view.TokenEditorView
import javafx.scene.paint.Color
import validated.*

/**
 * Editor for colors based on the [Color.web] function.
 */
@ProvideFeature
class ColorEditor(context: Context) : TokenEditor<Validated<String>, TokenEditorView>(context) {
    override fun compile(token: String): Validated<String> = try {
        Color.web(token)
        valid(token)
    } catch (ex: IllegalArgumentException) {
        invalid("Unrecognizable color string")
    }
}