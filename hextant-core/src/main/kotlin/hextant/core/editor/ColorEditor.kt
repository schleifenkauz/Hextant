/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.codegen.ProvideFeature
import hextant.context.Context
import javafx.scene.paint.Color
import validated.*

/**
 * Editor for colors based on the [Color.web] function.
 */
@ProvideFeature
class ColorEditor(context: Context) : CompletionTokenEditor<String>(context) {
    override fun compile(token: String): Validated<String> = try {
        Color.web(token)
        valid(token)
    } catch (ex: IllegalArgumentException) {
        invalid("Unrecognizable color string")
    }
}