/**
 * @author Nikolaus Knop
 */

package hextant.project.view

import bundles.SimpleProperty
import hextant.Editor
import org.controlsfx.glyphfont.FontAwesome.Glyph
import reaktive.value.ReactiveValue
import reaktive.value.reactiveValue

interface IconProvider<in E : Editor<*>> {
    fun provideIcon(editor: E): ReactiveValue<Glyph?>

    private object NoIconProvider : IconProvider<Editor<*>> {
        override fun provideIcon(editor: Editor<*>): ReactiveValue<Glyph?> = reactiveValue(null)
    }

    companion object {
        private val prop = SimpleProperty<IconProvider<*>>("icon provider", default = NoIconProvider)

        @Suppress("UNCHECKED_CAST")
        fun <E : Editor<*>> property() = prop as SimpleProperty<IconProvider<E>>
    }
}