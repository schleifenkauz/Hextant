/**
 *@author Nikolaus Knop
 */

package hextant.fx

import org.controlsfx.glyphfont.*

/**
 * Utility object for creating [Glyph]
 */
object Glyphs {
    private val font by lazy { GlyphFontRegistry.font("FontAwesome") }

    /**
     * Create a [Glyph] by the given [name]
     */
    fun create(name: String): Glyph = font.create(name)

    /**
     * Create a [Glyph] by the given [character]
     */
    fun create(character: Char): Glyph = font.create(character)

    /**
     * Create a [Glyph] by the given [FontAwesome.Glyph]
     */
    fun create(glyph: FontAwesome.Glyph): Glyph = font.create(glyph)
}

/**
 * Set the font size and return this glyph.
 * Useful for chained method calls.
 */
fun Glyph.fontSize(size: Double): Glyph {
    fontSize = size
    return this
}