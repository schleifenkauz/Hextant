/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.expr

import javafx.application.Application
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.layout.BorderPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import kserial.*
import org.nikok.hextant.*
import org.nikok.hextant.core.*
import org.nikok.hextant.core.command.Commands
import org.nikok.hextant.core.command.line.CommandLine
import org.nikok.hextant.core.command.line.FXCommandLineView
import org.nikok.hextant.core.command.register
import org.nikok.hextant.core.expr.editable.ExpandableExpr
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.expr.editor.*
import org.nikok.hextant.core.fx.hextantScene
import org.nikok.reaktive.value.now
import java.util.logging.Level

class ExprEditorViewTest : Application() {
    private lateinit var stage: Stage

    override fun start(primaryStage: Stage) {
        stage = primaryStage
        stage.scene = hextantScene(createContent())
        stage.setOnCloseRequest { System.exit(0) }
        stage.show()
    }

    private fun createContent(): Parent {
        val platform = HextantPlatform.INSTANCE.copy { }
        val views = platform[EditorViewFactory]
        val expandable = ExpandableExpr()
        val editors = platform[EditorFactory]
        val expander = editors.resolveEditor(expandable) as ExprExpander
        val commands = platform[Commands]
        val registrar = commands.of<ExprEditor>()
        registrar.register<ExprEditor, Int> {
            name = "Evaluate Expression"
            shortName = "eval"
            applicableIf { exprEditor -> exprEditor.expr != null }
            description = "Evaluates the selected expression and prints it to the console"
            executing { editor, _ ->
                val e = editor.expr!!
                val v = e.value
                v
            }
        }
        val expanderFactory = platform[ExpanderFactory]
        commands.of<OperatorEditor>().register<OperatorEditor, Unit> {
            name = "Flip operands"
            shortName = "flip_op"
            description = "Flips the both operands in this operator application"
            applicableIf { oe ->
                val oae = oe.parent as? OperatorApplicationEditor ?: return@applicableIf false
                oae.editable.editableOperator.edited.now?.isCommutative ?: false &&
                        oae.editable.editableOp1.editable.now != null &&
                        oae.editable.editableOp2.editable.now != null
            }
            executing { oe, _ ->
                val oae = oe.parent as OperatorApplicationEditor
                val expandableOp1 = oae.editable.editableOp1
                val editableOp1 = expandableOp1.editable.now!!
                val expandableOp2 = oae.editable.editableOp2
                val editableOp2 = expandableOp2.editable.now!!
                val expander1 = expanderFactory.getExpander(expandableOp1)
                expander1.setContent(editableOp2)
                val expander2 = expanderFactory.getExpander(expandableOp2)
                expander2.setContent(editableOp1)
            }
        }
        val expandableView = views.getFXView(expandable)
        val cl = CommandLine.forSelectedEditors(platform)
        val clView = FXCommandLineView(cl, platform)
        val menuBar = createMenuBar(expander)
        val split = SplitPane(menuBar, expandableView.root, clView)
        platform[CoreProperties.logger].level = Level.FINE
        split.orientation = VERTICAL
        return BorderPane(expandableView.root, menuBar, null, clView, null)
    }

    private fun createMenuBar(parent: ExprExpander): MenuBar {
        val save = createOpenBtn(parent)
        val open = createSaveBtn(parent)
        val file = Menu("File", null, save, open)
        return MenuBar(file)
    }

    private fun createSaveBtn(parent: ExprExpander) = MenuItem("Save").apply {
        setOnAction {
            val chooser = FileChooser()
            val file = chooser.showSaveDialog(stage) ?: return@setOnAction
            val out = serial.createOutput(file)
            out.writeObject(parent.editable.editable.now, context)
        }
        accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN)
    }

    private fun createOpenBtn(parent: ExprExpander) = MenuItem("Open").apply {
        setOnAction {
            val chooser = FileChooser()
            val file = chooser.showOpenDialog(stage) ?: return@setOnAction
            val input = serial.createInput(file)
            val editable = input.readTyped<Editable<Expr>>(context)!!
            parent.setContent(editable)
        }
        accelerator = KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN)
    }

    companion object {
        private val serial = KSerial.newInstance {}

        private val context = SerialContext.newInstance {}

        @JvmStatic fun main(args: Array<String>) {
            launch(ExprEditorViewTest::class.java, *args)
        }
    }
}
