/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.context.Context
import hextant.core.view.EditorControl
import hextant.fx.WindowSize
import hextant.fx.WindowSize.*
import hextant.fx.initHextantScene
import javafx.application.Platform
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

internal fun EditorControl<*>.receiveFocusLater() {
    Platform.runLater { receiveFocus() }
}

internal fun Stage.setScene(root: Parent, context: Context) {
    val sc = Scene(root)
    sc.initHextantScene(context)
    Platform.runLater { scene = sc }
}

internal fun Stage.setSize(s: WindowSize) {
    when (s) {
        Maximized -> isMaximized = true
        FullScreen -> isFullScreen = true
        FitContent -> {
        }
        is Configured -> {
            width = s.width
            height = s.height
        }
    }
}