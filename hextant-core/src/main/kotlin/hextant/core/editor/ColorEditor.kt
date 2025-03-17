/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.codegen.ProvideFeature
import hextant.codegen.RegisterEditor
import hextant.core.view.TokenEditorView
import javafx.scene.paint.Color
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.lang.reflect.Modifier
import java.util.*

/**
 * Editor for colors based on the [Color.web] function.
 */
@ProvideFeature
@RegisterEditor
class ColorEditor() : TokenEditor<Color, TokenEditorView>() {
    constructor(color: Color) : this() {
        setInitialText(toString(color) ?: "<???>")
    }

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
            .associate { (name, color) -> name.lowercase(Locale.getDefault()) to color as Color }

        private val colorNames = namedColors.entries.associate { (name, color) -> color to name }

        private fun getColor(token: String) = namedColors[token.lowercase(Locale.getDefault())] ?: Color.web(token)

        fun toString(color: Color) = colorNames[color]
    }
}