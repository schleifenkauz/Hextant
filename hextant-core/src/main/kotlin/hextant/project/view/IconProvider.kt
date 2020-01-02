/**
 * @author Nikolaus Knop
 */

package hextant.project.view

import hextant.Editor
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import org.controlsfx.glyphfont.FontAwesome.Glyph
import reaktive.value.ReactiveValue
import reaktive.value.reactiveValue

interface IconProvider<in E : Editor<*>> {
    fun provideIcon(editor: E): ReactiveValue<Glyph?>

    private object NoIconProvider : IconProvider<Editor<*>> {
        override fun provideIcon(editor: Editor<*>): ReactiveValue<Glyph?> = reactiveValue(null)
    }

    companion object {
        private val prop = Property<IconProvider<*>, Public, Public>("icon provider", default = NoIconProvider)

        @Suppress("UNCHECKED_CAST")
        fun <E : Editor<*>> property() = prop as Property<IconProvider<E>, Public, Public>
    }
}