/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.core.expr

import hextant.*
import hextant.bundle.CoreProperties
import hextant.command.Commands
import hextant.command.line.CommandLine
import hextant.command.line.FXCommandLineView
import hextant.command.register
import hextant.core.EditorControlFactory
import hextant.core.editor.Expander
import hextant.core.expr.editable.*
import hextant.core.expr.edited.*
import hextant.core.expr.editor.*
import hextant.core.list.*
import hextant.core.register
import hextant.fx.hextantScene
import hextant.inspect.*
import hextant.inspect.Severity.Warning
import hextant.undo.UndoManager
import hextant.undo.UndoManagerImpl
import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import kserial.*
import org.nikok.reaktive.value.now
import org.nikok.reaktive.value.observe
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
        val platform = HextantPlatform.configured()
        val context = object : AbstractContext(platform) {
            override val platform: HextantPlatform = platform
        }.apply {
            set(UndoManager, UndoManagerImpl())
        }
        val views = context[EditorControlFactory]
        val expandable = ExpandableExpr()
        val expander = context.getEditor(expandable) as ExprExpander
        val commands = context[Commands]
        val registrar = commands.of<ExprEditor>()
        views.register<EditableList<*, *>> { editable, ctx ->
            val editor = ctx.getEditor(editable)
            FXListEditorView(editable, ctx, editor as ListEditor<*>)
        }
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
        commands.of<OperatorEditor>().register<OperatorEditor, Unit> {
            name = "Flip operands"
            shortName = "flip_op"
            description = "Flips the both operands in this operator application"
            applicableIf { oe ->
                val oae = oe.parent as? OperatorApplicationEditor ?: return@applicableIf false
                oae.editable.editableOperator.edited.now?.isCommutative ?: false
            }
            executing { oe, _ ->
                val oae = oe.parent as OperatorApplicationEditor
                val expandableOp1 = oae.editable.editableOp1
                val editableOp1 = expandableOp1.editable.now
                val expandableOp2 = oae.editable.editableOp2
                val editableOp2 = expandableOp2.editable.now
                val expander1 = platform.getEditor(expandableOp1) as Expander<Editable<Expr>, *>
                if (editableOp2 != null) expander1.setContent(editableOp2)
                val expander2 = platform.getEditor(expandableOp2) as Expander<Editable<Expr>, *>
                if (editableOp1 != null) expander2.setContent(editableOp1)
            }
        }
        commands.of<OperatorApplicationEditor>().register<OperatorApplicationEditor, Unit> {
            name = "Collapse expression"
            shortName = "collapse"
            description = "Partially evaluate the selected expression"
            applicableIf { oae ->
                val ok = oae.editable.isOk.now
                val parentIsExpander = oae.parent is Expander<*, *>
                println("ok: $ok")
                println("parent: ${oae.parent}")
                ok && parentIsExpander
            }
            executing { oae, _ ->
                val ex = oae.parent as Expander<Editable<Expr>, *>
                val res = oae.editable.edited.now!!.value
                val editable = EditableIntLiteral(res)
                ex.setContent(editable)
            }
        }
        val inspections = context[Inspections]
        inspections.of<EditableOperatorApplication>().register { inspected ->
            inspection(inspected) {
                description = "Prevent identical operations"
                severity(Warning)
                val isPlus = inspected.editableOperator.edited.map("operator is +") { it == Operator.Plus }
                val operandIs0 = inspected.editableOp1.edited.map("operand is 0") { it is IntLiteral && it.value == 0 }
                preventingThat(isPlus.flatMap("error") { isP -> operandIs0.map("error") { it && isP } })
                message { "Operation doesn't change the result" }
            }
        }
        inspections.of<EditableIntLiteral>().register { inspected ->
            inspection(inspected) {
                description = "Prevent '0' Literals"
                message { "Literal is '0'" }
                severity(Warning)
                preventingThat(inspected.edited.map("is 0") { it?.value == 0 })
                addFix {
                    description = "Set to '1'"
                    fixingBy {
                        val editor = context.getEditor(inspected) as IntLiteralEditor
                        editor.setText("1")
                    }
                }
            }
        }
        val expanderView = context.createView(expandable)
        val cl = CommandLine.forSelectedEditors(platform)
        val clView = FXCommandLineView(cl, platform)
        val evaluationDisplay = Label("Invalid expression")
        val obs = expandable.edited.observe("Evaluation display") { _, _, new ->
            Platform.runLater {
                if (new == null) {
                    evaluationDisplay.text = "Invalid expression"
                } else {
                    val v = new.value
                    evaluationDisplay.text = "$v"
                }
            }
        }
        val menuBar = createMenuBar(expander, context)
        val split = SplitPane(menuBar, expanderView, clView)
        context[CoreProperties.logger].level = Level.FINE
        split.orientation = VERTICAL
        return BorderPane(
            HBox(10.0, expanderView, Label("->"), evaluationDisplay),
            menuBar,
            null,
            clView,
            null
        ).also {
            it.userData = obs
        }
    }

    private fun createMenuBar(
        parent: ExprExpander,
        context: Context
    ): MenuBar {
        val save = createOpenBtn(parent)
        val open = createSaveBtn(parent)
        val file = Menu("File", null, save, open)
        val undo = context[UndoManager]
        val edit = Menu("Edit", null, undoBtn(undo), redoBtn(undo))
        return MenuBar(file, edit)
    }

    private fun redoBtn(undo: UndoManager): MenuItem = MenuItem("Undo").apply {
        setOnAction {
            if (undo.canUndo) {
                undo.undo()
            }
        }
        accelerator = KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN)
    }

    private fun undoBtn(undo: UndoManager): MenuItem = MenuItem("Redo").apply {
        setOnAction {
            if (undo.canRedo) {
                undo.redo()
            }
        }
        accelerator = KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
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

        private val context = SerialContext.newInstance {
            classLoader = ExprEditorViewTest::class.java.classLoader
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(ExprEditorViewTest::class.java, *args)
        }
    }
}
