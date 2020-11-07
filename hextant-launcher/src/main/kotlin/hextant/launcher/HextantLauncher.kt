package hextant.launcher

import bundles.set
import hextant.command.line.CommandLine
import hextant.command.line.CommandLineControl.Companion.HISTORY_ITEMS
import hextant.command.line.SingleCommandSource
import hextant.context.Context
import hextant.context.createControl
import hextant.fx.*
import hextant.launcher.HextantPlatform.launcher
import hextant.launcher.HextantPlatform.stage
import hextant.settings.Settings
import javafx.geometry.Pos.CENTER
import javafx.scene.control.Label
import javafx.scene.text.Font
import reaktive.value.fx.asObservableValue

internal class HextantLauncher(global: Context, private val context: Context) : Runnable {
    init {
        global[launcher] = this
    }

    private val commandLine = run {
        val src = SingleCommandSource(context, context)
        val cl = CommandLine(context, src)
        context.createControl(cl) { set(HISTORY_ITEMS, 1) }
    }

    private val root = hbox {
        setPrefSize(400.0, 400.0)
        alignment = CENTER
        add(vbox()) {
            setPrefSize(200.0, 400.0)
            alignment = CENTER
            spacing = 30.0
            add(label()) {
                val header = context[Settings].getReactive(HextantMain.Header)
                textProperty().bind(header.asObservableValue())
                font = Font(24.0)
            }
            add(commandLine)
        }
    }

    override fun run() {
        val stage = context[stage]
        if (root.scene != null) root.scene.root = Label()
        stage.setScene(root, context)
        commandLine.receiveFocusLater()
    }
}

