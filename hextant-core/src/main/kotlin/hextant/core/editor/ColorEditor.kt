/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.codegen.ProvideFeature
import hextant.codegen.RegisterEditor
import hextant.context.Context
import hextant.core.view.TokenEditorView
import javafx.scene.paint.Color
import java.lang.reflect.Modifier

/**
 * Editor for colors based on the [Color.web] function.
 */
@ProvideFeature
@RegisterEditor
class ColorEditor(context: Context, text: String = "") : TokenEditor<Color, TokenEditorView>(context, text) {
    constructor(context: Context, color: Color) : this(context, toString(color) ?: "<???>")

    override fun compile(token: String): Color = try {
        getColor(token)
    } catch (ex: IllegalArgumentException) {
        Color.BLACK
    }

    companion object {
        private val namedColors = Color::class.java.declaredFields
            .filter { f -> Modifier.isPublic(f.modifiers) && Modifier.isStatic(f.modifiers) }
            .map { f -> f.name to f.get(null) }
            .filter { (_, value) -> value is Color }
            .associate { (name, color) -> name.toLowerCase() to color as Color }

        private val colorNames = namedColors.entries.associate { (name, color) -> color to name }

        private fun getColor(token: String) = namedColors[token.toLowerCase()] ?: Color.web(token)

        fun toString(color: Color) = colorNames[color]
    }
}