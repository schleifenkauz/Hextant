/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.core.EditorView
import java.nio.file.Path

/**
 * Displays [PathEditor]
 */
interface PathEditorView : EditorView {
    /**
     * Show the given [path] to the user in some manner.
     */
    fun displayPath(path: Path)
}