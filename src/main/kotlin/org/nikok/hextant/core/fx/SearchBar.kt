package org.nikok.hextant.core.fx

import org.controlsfx.control.textfield.CustomTextField
import org.controlsfx.glyphfont.FontAwesome.Glyph.CLOSE
import org.controlsfx.glyphfont.FontAwesome.Glyph.SEARCH

class SearchBar : CustomTextField() {
    init {
        left = Glyphs.create(SEARCH).fontSize(18.0)
        right = Glyphs.create(CLOSE).fontSize(18.0)
        promptText = "Search..."
        styleClass.add("hextant-search-bar")
    }
}
