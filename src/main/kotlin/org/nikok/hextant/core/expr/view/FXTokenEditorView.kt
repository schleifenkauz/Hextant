/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.view

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.EditorFactory
import org.nikok.hextant.core.editable.EditableToken
import org.nikok.hextant.core.editor.TokenEditor
import org.nikok.hextant.core.fx.*
import org.nikok.hextant.get

open class FXTokenEditorView(
    editable: EditableToken<Any>,
    platform: HextantPlatform
) : FXEditorView, TextEditorView {
    final override val node = HextantTextField().apply {
        textProperty().addListener { _, _, new -> editor.setText(new) }
    }

    private val editor = platform[EditorFactory].resolveEditor(editable) as TokenEditor<Any>

    init {
        node.initSelection(editor)
        node.activateInspections(editable, platform)
        node.activateContextMenu(editor, platform)
        editor.addView(this)
    }

    final override fun displayText(t: String) {
        node.smartSetText(t)
    }
}