/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.context.Context
import hextant.context.Internal
import javafx.stage.DirectoryChooser
import java.nio.file.Path

/**
 * Implements the [PathChooser] interface using the JavaFX Gui toolkit.
 */
class FXPathChooser : PathChooser {
    private val dc = DirectoryChooser()

    init {
        dc.title = "Choose project"
    }

    override fun choosePath(context: Context): Path? {
        val stage = context[Internal, HextantApplication.stage]
        val file = dc.showDialog(stage) ?: return null
        return file.toPath()
    }
}