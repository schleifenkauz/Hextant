/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.Context
import hextant.get
import javafx.stage.DirectoryChooser
import java.nio.file.Path

class FXPathChooser : PathChooser {
    private val dc = DirectoryChooser()

    init {
        dc.title = "Choose project"
    }

    override fun choosePath(context: Context): Path? {
        val stage = context[HextantApplication.stage]
        val file = dc.showDialog(stage) ?: return null
        return file.toPath()
    }
}