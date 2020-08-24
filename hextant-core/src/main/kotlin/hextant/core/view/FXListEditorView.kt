/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.editor.ListEditor
import hextant.fx.registerShortcuts
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode.MULTIPLE
import reaktive.list.fx.asObservableList

/**
 * An [EditorControl] that uses a [ListView] to display a [ListEditor]
 */
class FXListEditorView(private val editor: ListEditor<*, *>, arguments: Bundle) :
    EditorControl<ListView<Editor<*>>>(editor, arguments) {
    override fun createDefaultRoot(): ListView<Editor<*>> = ListView(editor.editors.asObservableList()).apply {
        setCellFactory { Cell() }
        selectionModel.selectionMode = MULTIPLE
        registerShortcuts {
            on("Delete") {
                val idx = selectionModel.selectedIndex
                if (idx != -1) {
                    editor.removeAt(idx)
                }
            }
            on("Insert") {
                val idx = selectionModel.selectedIndex
                editor.addAt(idx + 1) //also works if idx == -1
            }
            on("Shift+Insert") {
                val idx = selectionModel.selectedIndex
                if (idx != -1) editor.addAt(idx)
            }
        }
    }

    private inner class Cell : ListCell<Editor<*>>() {
        override fun updateItem(item: Editor<*>?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == null || empty) graphic = null else {
                val v = context.createControl(item)
                graphic = v
                v.receiveFocus()
            }
        }
    }
}