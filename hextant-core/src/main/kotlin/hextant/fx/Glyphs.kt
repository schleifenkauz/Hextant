/**
 *@author Nikolaus Knop
 */

package hextant.fx

import org.controlsfx.glyphfont.*

object Glyphs {
    private val font by lazy { GlyphFontRegistry.font("FontAwesome") }

    fun create(name: String): Glyph = font.create(name)

    fun create(character: Char): Glyph = font.create(character)

    fun create(glyph: FontAwesome.Glyph): Glyph = font.create(glyph)
}

fun Glyph.fontSize(size: Double): Glyph {
    fontSize = size
    return this
}