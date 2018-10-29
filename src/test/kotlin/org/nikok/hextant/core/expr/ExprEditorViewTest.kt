/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.expr

import javafx.application.Application
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.SplitPane
import javafx.stage.Stage
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.*
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.command.Commands
import org.nikok.hextant.core.command.line.CommandLine
import org.nikok.hextant.core.command.line.FXCommandLineView
import org.nikok.hextant.core.command.register
import org.nikok.hextant.core.expr.editable.ExpandableExpr
import org.nikok.hextant.core.expr.editor.*
import org.nikok.hextant.core.fx.lastShortcutLabel
import org.nikok.hextant.core.fx.scene
import org.nikok.reaktive.value.now
import java.util.logging.Level

class ExprEditorViewTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = scene(Label())
        stage.scene.root = createContent(stage.scene)
        stage.setOnCloseRequest { System.exit(0) }
        stage.show()
    }

    companion object {
        private fun createContent(scene: Scene): Parent {
            val views = HextantPlatform[Public, EditorViewFactory]
            val expandable = ExpandableExpr()
            val commands = HextantPlatform[Public, Commands]
            val registrar = commands.of<ExprEditor>()
            val editors = HextantPlatform[Public, EditorFactory]
            editors.registerExpander { e: ExpandableExpr -> ExprExpander(e) }
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
            val expanderFactory = HextantPlatform[Public, ExpanderFactory]
            commands.of<OperatorApplicationEditor>().register<OperatorApplicationEditor, Unit> {
                name = "Flip operands"
                shortName = "flip_op"
                applicableIf { oae ->
                    oae.editable.editableOperator.edited.now?.isCommutative ?: false &&
                    oae.editable.editableOp1.editable.now != null &&
                    oae.editable.editableOp2.editable.now != null
                }
                description = "Flips the both operands in this operator application"
                executing { editor, _ ->
                    val expandableOp1 = editor.editable.editableOp1
                    val editableOp1 = expandableOp1.editable.now!!
                    val expandableOp2 = editor.editable.editableOp2
                    val editableOp2 = expandableOp2.editable.now!!
                    val expander1 = expanderFactory.getExpander(expandableOp1)
                    expander1.setContent(editableOp2)
                    val expander2 = expanderFactory.getExpander(expandableOp2)
                    expander2.setContent(editableOp1)
                    if (!editor.isSelected) editor.toggleSelection()
                }
            }
            val expandableView = views.getFXView(expandable)
            val cl = CommandLine.forSelectedEditors()
            val clView = FXCommandLineView(cl)
            val split = SplitPane(expandableView.node, clView, lastShortcutLabel(scene))
            HextantPlatform[Public, CoreProperties.logger].level = Level.FINE
            split.orientation = VERTICAL
            return split
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(ExprEditorViewTest::class.java, *args)
        }
    }
}
