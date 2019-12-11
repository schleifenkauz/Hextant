/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import hextant.EditorControlGroup
import hextant.base.EditorControl
import hextant.blocky.editor.ExecutableEditor
import hextant.blocky.editor.NextExecutableEditor
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import javafx.scene.Parent
import javafx.scene.control.*
import reaktive.value.now

abstract class ExecutableEditorControl<R : Parent>(private val editor: ExecutableEditor<*>, args: Bundle) :
    EditorControl<R>(editor, args) {
    private val parentView
        get() = generateSequence<Parent>(this) { it.parent }
            .first { it is ProgramEditorControl } as ProgramEditorControl

    protected fun configureArrowTarget() {
        val item = MenuItem("Arrow Target")
        item.setOnAction {
            val origin = arrowStart ?: return@setOnAction
            origin.setNext(editor)
            val from = origin.parent.now!!
            val controlGroup = context[Public, EditorControlGroup]
            val originView = controlGroup.getViewOf(from)
            parentView.displayArrow(originView, this)
        }
        if (contextMenu == null) contextMenu = ContextMenu()
        contextMenu.items.add(item)
    }

    protected fun Control.configureArrowStart(start: NextExecutableEditor) {
        val item = MenuItem("Arrow Start")
        item.setOnAction { ev ->
            arrowStart = start
            ev.consume()
        }
        if (contextMenu == null) contextMenu = ContextMenu()
        contextMenu.items.add(item)
    }

    companion object {
        private var arrowStart: NextExecutableEditor? = null
    }
}