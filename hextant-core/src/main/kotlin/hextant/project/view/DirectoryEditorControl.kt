/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.core.view.FXListEditorView
import hextant.createView
import hextant.fx.*
import hextant.project.editor.DirectoryEditor
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import org.controlsfx.glyphfont.FontAwesome.Glyph.FOLDER
import org.controlsfx.glyphfont.FontAwesome.Glyph.FOLDER_OPEN

class DirectoryEditorControl(
    editor: DirectoryEditor<*>,
    arguments: Bundle
) : EditorControl<VBox>(editor, arguments) {
    private var expanded = false
    private val iconClosed = Glyphs.create(FOLDER).fontSize(20.0).color(Color.YELLOW)
    private val iconOpen = Glyphs.create(FOLDER_OPEN).fontSize(20.0).color(Color.YELLOW)
    val directoryName = context.createView(editor.directoryName)
    private val cell = HBox(4.0, iconClosed, directoryName)
    private val items = context.createView(editor.items) {
        set(Public, FXListEditorView.ORIENTATION, FXListEditorView.Orientation.Vertical)
        set(Public, FXListEditorView.CELL_FACTORY) { FXListEditorView.PrefixCell(Label("    ")) }
    }

    init {
        setChildren(directoryName)
        onAction {
            if (expanded) hide()
            else expand()
        }
    }

    override fun receiveFocus() {
        directoryName.receiveFocus()
    }

    fun expand() {
        if (expanded) return
        cell.children[0] = iconOpen
        root.children.add(items)
        expanded = true
    }

    fun hide() {
        if (!expanded) return
        cell.children[0] = iconClosed
        root.children.remove(items)
        expanded = false
    }

    override fun createDefaultRoot(): VBox = VBox(cell)
}