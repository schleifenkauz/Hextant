/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.view

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.EditorFactory
import org.nikok.hextant.core.base.EditorControl
import org.nikok.hextant.core.editable.EditableToken
import org.nikok.hextant.core.editor.TokenEditor
import org.nikok.hextant.core.fx.HextantTextField
import org.nikok.hextant.core.fx.smartSetText
import org.nikok.hextant.core.get

open class FXTokenEditorView(
    editable: EditableToken<Any>,
    platform: HextantPlatform
) : EditorControl<HextantTextField>(), TextEditorView {
    final override fun createDefaultRoot() = HextantTextField().apply {
        textProperty().addListener { _, _, new -> editor.setText(new) }
    }

    @Suppress("UNCHECKED_CAST")
    private val editor = platform[EditorFactory].resolveEditor(editable) as TokenEditor<*, TextEditorView>

    init {
        initialize(editable, editor, platform)
        editor.addView(this)
    }

    final override fun displayText(t: String) {
        root.smartSetText(t)
    }
}