/**
 *@author Nikolaus Knop
 */

package hextant.fx

import hextant.context.Context
import hextant.context.createControl
import hextant.core.Editor
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.StageStyle.DECORATED

internal class HextantStage(context: Context, root: Parent, style: StageStyle = DECORATED) : Stage(style) {
    constructor(editor: Editor<*>, style: StageStyle = DECORATED) :
            this(editor.context, editor.context.createControl(editor), style)

    init {
        scene = Scene(root)
        context[Stylesheets].manage(scene)
    }
}