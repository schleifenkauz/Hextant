/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.fx.on
import hextant.fx.registerShortcuts
import hextant.get
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.input.KeyCode.F2
import java.nio.file.Path

class PathEditorControl(
    val editor: PathEditor,
    arguments: Bundle
) : EditorControl<Button>(editor, arguments), PathEditorView {
    init {
        registerShortcuts {
            on(F2) {
                rename()
            }
        }
        editor.addView(this)
    }

    private fun rename() {
        val path = context[PathChooser].choosePath(context)
        if (path != null) editor.choosePath(path)
    }

    override fun createDefaultRoot() = Button().apply {
        prefWidth = 250.0
        alignment = Pos.CENTER_LEFT
        styleClass.add("path")
    }

    override fun displayPath(path: Path) {
        root.text = path.toString()
    }
}