/**
 *@author Nikolaus Knop
 */

package hextant.core.fx

import org.controlsfx.glyphfont.*

object Glyphs {
    private val font by lazy { GlyphFontRegistry.font("FontAwesome") }

    fun create(name: String) = font.create(name)

    fun create(character: Char) = font.create(character)

    fun create(glyph: FontAwesome.Glyph) = font.create(glyph)
}

fun Glyph.fontSize(size: Double): Glyph {
    fontSize = size
    return this
}