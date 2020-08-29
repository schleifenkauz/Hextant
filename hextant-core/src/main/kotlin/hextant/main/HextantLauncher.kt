package hextant.main

import hextant.command.line.CommandLine
import hextant.command.line.SingleCommandSource
import hextant.context.createControl
import hextant.fx.*
import hextant.main.HextantApp.Companion.stage
import hextant.main.HextantPlatform.launcherContext
import javafx.geometry.Pos.CENTER
import javafx.scene.text.Font

internal class HextantLauncher : Runnable {
    private val commandLine = run {
        val receiver = launcherContext[ProjectManager]
        val src = SingleCommandSource(launcherContext, receiver)
        val cl = CommandLine(launcherContext, src)
        launcherContext.createControl(cl)
    }

    private val root = hbox {
        setPrefSize(400.0, 400.0)
        alignment = CENTER
        add(vbox()) {
            setPrefSize(200.0, 400.0)
            alignment = CENTER
            spacing = 30.0
            add(label("Hextant")) {
                font = Font(24.0)
            }
            add(commandLine)
        }
    }

    override fun run() {
        val stage = launcherContext[stage]
        stage.setScene(root, launcherContext)
        commandLine.receiveFocusLater()
    }
}

