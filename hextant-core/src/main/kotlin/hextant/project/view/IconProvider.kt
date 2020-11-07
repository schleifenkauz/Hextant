/**
 * @author Nikolaus Knop
 */

package hextant.project.view

import bundles.PublicProperty
import bundles.publicProperty
import hextant.core.Editor
import org.controlsfx.glyphfont.FontAwesome.Glyph
import reaktive.value.ReactiveValue
import reaktive.value.reactiveValue

/**
 * An [IconProvider] is responsible for providing the right icon for a given editor.
 */
interface IconProvider<in E : Editor<*>> {
    /**
     * Return a [ReactiveValue] that always holds the appropriate [Glyph] for displaying the given [editor]
     * or `null` if there should be not glyph displayed.
     */
    fun provideIcon(editor: E): ReactiveValue<Glyph?>

    private object NoIconProvider : IconProvider<Editor<*>> {
        override fun provideIcon(editor: Editor<*>): ReactiveValue<Glyph?> = reactiveValue(null)
    }

    companion object {
        private val prop = publicProperty<IconProvider<*>>("icon provider", NoIconProvider)

        /**
         * This property is used to
         */
        @Suppress("UNCHECKED_CAST")
        fun <E : Editor<*>> property() = prop as PublicProperty<IconProvider<E>>
    }
}