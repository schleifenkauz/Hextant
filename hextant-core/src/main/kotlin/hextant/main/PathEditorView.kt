/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.EditorView
import java.nio.file.Path

interface PathEditorView : EditorView {
    fun displayPath(path: Path)
}