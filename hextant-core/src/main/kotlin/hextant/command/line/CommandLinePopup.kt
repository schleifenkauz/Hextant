/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import bundles.createBundle
import hextant.context.Context
import hextant.fx.HextantPopup

internal class CommandLinePopup(context: Context, commandLine: CommandLine, minWidth: Double = 300.0) :
    HextantPopup(context) {
    private val autoHide = commandLine.executedCommand.observe { _, _ -> hide() }
    private val view = CommandLineControl(commandLine, createBundle())


    init {
        view.minWidth = minWidth
        scene.root = view
    }

    override fun show() {
        super.show()
        view.receiveFocus()
    }
}