package hextant.launcher.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.command.line.CommandLine
import hextant.command.line.CommandLineControl.Companion.HISTORY_ITEMS
import hextant.command.line.SingleCommandSource
import hextant.config.Settings
import hextant.context.ControlFactory
import hextant.context.createControl
import hextant.core.view.EditorControl
import hextant.fx.add
import hextant.fx.hbox
import hextant.fx.label
import hextant.fx.vbox
import hextant.launcher.HextantLauncher
import hextant.launcher.Launcher
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import reaktive.value.fx.asObservableValue

class CommandLineBasedLauncherView @ProvideImplementation(ControlFactory::class) constructor(
    editor: Launcher,
    parameters: Bundle
) : LauncherView, EditorControl<HBox>(editor, parameters) {
    private val commandLine = createCommandLine()

    override fun createDefaultRoot(): HBox = hbox {
        setPrefSize(600.0, 600.0)
        alignment = Pos.CENTER
        add(vbox()) {
            setPrefSize(400.0, 400.0)
            alignment = Pos.CENTER
            spacing = 30.0
            add(label()) {
                val header = context[Settings].getReactive(HextantLauncher.Header)
                textProperty().bind(header.asObservableValue())
                font = Font(24.0)
            }
            add(commandLine)
        }
    }

    private fun createCommandLine(): EditorControl<*> {
        val src = SingleCommandSource(context, context)
        val cl = CommandLine.create(context, src)
        return context.createControl(cl) { set(HISTORY_ITEMS, 1) }
    }

    override fun receiveFocus() {
        commandLine.receiveFocus()
    }

    override fun requestFocus() {
        receiveFocus()
    }
}
