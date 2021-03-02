/**
 * @author Nikolaus Knop
 */

package hextant.launcher

import hextant.context.Context
import hextant.context.EditorControlGroup
import hextant.core.EditorView
import hextant.core.view.EditorControl
import hextant.fx.*
import hextant.fx.WindowSize.*
import javafx.application.Platform
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

internal fun Stage.setScene(root: Parent, context: Context) {
    val sc = Scene(root)
    sc.initHextantScene(context)
    Platform.runLater { scene = sc }
}

internal fun Stage.setSize(s: WindowSize) {
    when (s) {
        Maximized -> isMaximized = true
        FullScreen -> isFullScreen = true
        Default -> {
            width = scene.width
            height = scene.height
        }
        FitContent -> {
        }
        is Configured -> {
            width = s.width
            height = s.height
        }
    }
}

internal fun setTitleAndFocus(stage: Stage, title: String, view: EditorControl<*>) = runFXWithTimeout {
    stage.title = title
    view.receiveFocus()
}