/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.codegen.ProvideFeature
import hextant.codegen.ProvideImplementation
import hextant.context.Context
import hextant.context.EditorFactory
import javafx.scene.paint.Color
import validated.*

@ProvideFeature
internal class ColorEditor @ProvideImplementation(EditorFactory::class) constructor(context: Context) :
    CompletionTokenEditor<Color>(context) {
    override fun compile(token: String): Validated<Color> = try {
        valid(Color.web(token))
    } catch (ex: IllegalArgumentException) {
        invalid("Unrecognizable color string")
    }
}