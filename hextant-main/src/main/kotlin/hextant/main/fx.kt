package hextant.main

import hextant.context.Context
import hextant.core.view.EditorControl
import hextant.fx.WindowSize
import hextant.fx.initHextantScene
import hextant.fx.runFXWithTimeout
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
        WindowSize.Maximized -> isMaximized = true
        WindowSize.FullScreen -> isFullScreen = true
        WindowSize.Default -> {
            width = scene.width
            height = scene.height
        }
        WindowSize.FitContent -> {
        }
        is WindowSize.Configured -> {
            width = s.width
            height = s.height
        }
    }
}

internal fun setTitleAndFocus(stage: Stage, title: String, view: EditorControl<*>) = runFXWithTimeout {
    stage.title = title
    view.receiveFocus()
}