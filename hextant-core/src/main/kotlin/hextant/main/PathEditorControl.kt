/**
 *@author Nikolaus Knop
 */

package hextant.main

import bundles.Bundle
import hextant.core.Internal
import hextant.fx.EditorControl
import hextant.fx.registerShortcuts
import javafx.geometry.Pos
import javafx.scene.control.Button
import java.nio.file.Path

class PathEditorControl(
    val editor: PathEditor,
    arguments: Bundle
) : EditorControl<Button>(editor, arguments), PathEditorView {
    init {
        registerShortcuts {
            on("F2") {
                rename()
            }
        }
        editor.addView(this)
    }

    private fun rename() {
        val path = context[Internal, PathChooser].choosePath(context)
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