/**
 * @author Nikolaus Knop
 */

package hextant.expr

import hextant.*
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.bundle.CoreProperties
import hextant.command.Commands
import hextant.command.line.CommandLine
import hextant.command.line.FXCommandLineView
import hextant.command.register
import hextant.core.EditorControlFactory
import hextant.core.list.*
import hextant.core.register
import hextant.expr.editable.*
import hextant.expr.edited.*
import hextant.expr.edited.Operator.Plus
import hextant.expr.editor.*
import hextant.fx.hextantScene
import hextant.impl.SelectionDistributor
import hextant.inspect.Inspections
import hextant.inspect.Severity.Warning
import hextant.inspect.of
import hextant.undo.UndoManager
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
import reaktive.Observer
import reaktive.value.binding.and
import reaktive.value.binding.map
import reaktive.value.now
import java.util.logging.Level

class ExprEditorViewTest : Application() {
    private lateinit var stage: Stage

    override fun start(primaryStage: Stage) {
        stage = primaryStage
        stage.scene = hextantScene(::createContent) { platform ->
            Context.newInstance(platform) {
                set(Public, UndoManager, UndoManager.concurrent())
                set(Public, SelectionDistributor, SelectionDistributor.newInstance())
            }
        }
        stage.setOnCloseRequest { System.exit(0) } //Needed to stop the daemon threads
        stage.show()
    }

    private fun createContent(context: Context): Parent {
        context[EditorControlFactory].register<EditableList<*, *>> { editable, ctx, args ->
            val editor = ctx.getEditor(editable)
            FXListEditorView(editable, ctx, editor as ListEditor<*>, bundle = args)
        }
        registerCommandsAndInspections(context)
        val expandable = ExpandableExpr()
        val expander = context.getEditor(expandable) as ExprExpander
        val expanderView = context.createView(expandable)
        val clContext = Context.newInstance(context) {
            set(Public, SelectionDistributor, SelectionDistributor.newInstance())
        }
        val sd = context[SelectionDistributor]
        val cl = CommandLine.forSelectedEditors(sd, clContext)
        val clView = FXCommandLineView(cl, clContext, Bundle.newInstance())
        val (evaluationDisplay, obs) = evaluateOnExprChange(expandable)
        val menuBar = createMenuBar(expander, context)
        val split = SplitPane(menuBar, expanderView, clView)
        context[CoreProperties.logger].level = Level.INFO
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

    private fun evaluateOnExprChange(expandable: ExpandableExpr): Pair<Label, Observer> {
        val evaluationDisplay = Label("Invalid expression")
        val obs = expandable.result.observe { _, _, new ->
            Platform.runLater {
                if (new !is Ok) {
                    evaluationDisplay.text = "Invalid expression"
                } else {
                    val v = new.value
                    evaluationDisplay.text = "$v"
                }
            }
        }
        return Pair(evaluationDisplay, obs)
    }

    private fun registerCommandsAndInspections(context: Context) {
        val commands = context[Commands]
        commands.of<ExprEditor>().register<ExprEditor, Int> {
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
                oae.editable.editableOperator.result.now.map { it.isCommutative }.default { false }
            }
            executing { oe, _ ->
                val oae = oe.parent as OperatorApplicationEditor
                val expandableOp1 = oae.editable.editableOp1
                val editableOp1 = expandableOp1.editable.now
                val expandableOp2 = oae.editable.editableOp2
                val editableOp2 = expandableOp2.editable.now
                val expander1: ExprExpander = context.getEditor(expandableOp1)
                if (editableOp2 != null) expander1.setContent(editableOp2)
                val expander2: ExprExpander = context.getEditor(expandableOp2)
                if (editableOp1 != null) expander2.setContent(editableOp1)
            }
        }
        commands.of<OperatorApplicationEditor>().register<OperatorApplicationEditor, Unit> {
            name = "Collapse expression"
            shortName = "collapse"
            description = "Partially evaluate the selected expression"
            applicableIf { oae ->
                oae.editable.isOk && oae.expander != null
            }
            executing { oae, _ ->
                val ex = oae.expander as ExprExpander
                val res = oae.editable.result.now.force().value
                val editable = EditableIntLiteral(res)
                ex.setContent(editable)
            }
        }
        commands.of<ExprEditor>().register<ExprEditor, Unit> {
            name = "Unwrap expression"
            shortName = "unwrap"
            description = "Unwrap an expression by replacing its outer application with itself"
            applicableIf {
                it is Editor<*> && it.parent is OperatorApplicationEditor && it.parent!!.expander is ExprExpander
            }
            executing { editor, _ ->
                editor as Editor<*>
                val parentExpander = editor.parent!!.expander as ExprExpander
                parentExpander.setContent(editor.editable as EditableExpr<*>)
            }
        }
        val inspections = context[Inspections]
        inspections.of<EditableOperatorApplication>().registerInspection { inspected ->
            description = "Prevent identical operations"
            severity(Warning)
            val isPlus = inspected.editableOperator.result.map { it.defaultNull() == Plus }
            val operandIs0 =
                inspected.editableOp2.result.map { it.defaultNull() is IntLiteral && it.force().value == 0 }
            preventingThat(isPlus.and(operandIs0))
            message { "Operation doesn't change the result" }
            addFix {
                description = "Shorten expression"
                applicableIf {
                    context.getEditor(inspected).expander is ExprExpander
                }
                fixingBy {
                    val parent = context.getEditor(inspected).expander as ExprExpander
                    parent.setContent(inspected.editableOp1)
                }

            }
        }
        inspections.of<EditableIntLiteral>().registerInspection { inspected ->
            description = "Prevent '0' Literals"
            message { "Literal is '0'" }
            severity(Warning)
            preventingThat(inspected.result.map { it.defaultNull()?.value == 0 })
            addFix {
                description = "Set to '1'"
                fixingBy {
                    val editor: IntLiteralEditor = context.getEditor(inspected)
                    editor.setText("1")
                }
            }
        }
        commands.of<ExprEditor>().register<ExprEditor, Unit> {
            description =
                "Wraps the current expression in an operator expression with the current expression being the left operand"
            name = "Wrap in operator expression"
            shortName = "wrap_op"
            addParameter {
                ofType<Operator>()
                description = "The operator being applied"
                name = "operator"
            }
            applicableIf { it is Editor<*> && it.expander is ExprExpander }
            executing { editor, (operator) ->
                operator as Operator
                editor as Editor<*>
                val parent = editor.expander as ExprExpander
                val leftSide = editor.editable as EditableExpr<*>
                val editableOp = EditableOperator(operator)
                val app = EditableOperatorApplication(editableOp, ExpandableExpr(leftSide), ExpandableExpr())
                parent.setContent(app)
            }
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
