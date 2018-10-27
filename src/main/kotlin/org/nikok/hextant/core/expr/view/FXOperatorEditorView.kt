/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.view

import javafx.scene.Node
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.expr.editable.EditableOperator
import org.nikok.hextant.core.expr.editor.OperatorEditor
import org.nikok.hextant.core.fx.*

class FXOperatorEditorView(editable: EditableOperator): FXEditorView {
    override val node: Node

    init {
        val views = HextantPlatform[Public, EditorViewFactory]
        val view = views.getFXView(editable.editableText)
        node = view.node
        node.activateInspections(editable)
        val editor = OperatorEditor(editable, this)
        node.activateContextMenu(editor)
        initSelection(editor)
    }
}