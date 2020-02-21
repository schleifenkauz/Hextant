package hextant.fx

import org.controlsfx.control.textfield.CustomTextField
import org.controlsfx.glyphfont.FontAwesome.Glyph.SEARCH

/**
 * A [CustomTextField] with the [SEARCH]-glyph and the 'hextant-search-bar' style class.
 */
class SearchBar : CustomTextField() {
    init {
        left = Glyphs.create(SEARCH).fontSize(18.0)
        promptText = "Search..."
        styleClass.add("hextant-search-bar")
    }
}
