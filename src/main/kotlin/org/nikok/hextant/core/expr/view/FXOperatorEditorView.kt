/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.view

import javafx.scene.Node
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.expr.editable.EditableOperator
import org.nikok.hextant.core.expr.editor.OperatorEditor
import org.nikok.hextant.core.fx.*
import org.nikok.hextant.get

class FXOperatorEditorView(editable: EditableOperator, platform: HextantPlatform) : FXEditorView {
    override val node: Node

    init {
        val views = platform[EditorViewFactory]
        val view = views.getFXView(editable.editableText)
        node = view.node
        node.activateInspections(editable, platform)
        val editor = OperatorEditor(editable, platform)
        node.activateContextMenu(editor, platform)
    }
}