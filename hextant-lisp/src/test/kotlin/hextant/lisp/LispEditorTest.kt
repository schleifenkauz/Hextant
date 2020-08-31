package hextant.lisp

import hextant.context.Context
import hextant.context.createControl
import hextant.lisp.editor.LispProperties
import hextant.lisp.editor.SExprExpander
import hextant.lisp.view.Properties.editorParentRegion
import hextant.main.HextantApplication
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import reaktive.value.now
import validated.orNull

////(let (square (lambda (x) (* x x))) (+ (square 12) (square 8)))
//val expr2 = let(
//    listOf(
//        "square" to LambdaExpr(
//            listOf("x"), Apply(
//                GetVal("*", scope),
//                listOf(
//                    GetVal("x", scope),
//                    GetVal("x", scope)
//                )
//            )
//        )
//    ),
//    Apply(
//        GetVal("+", scope),
//        listOf(
//            Apply(
//                GetVal("square", scope),
//                listOf(
//                    IntLiteral(12)
//                )
//            ),
//            Apply(
//                GetVal("square", scope),
//                listOf(
//                    IntLiteral(8)
//                )
//            )
//        )
//    )
//)
class LispEditorTest : HextantApplication() {
    override fun Context.initializeContext() {
        set(LispProperties.fileScope, FileScope.empty)
    }

    override fun createView(context: Context): Parent {
        val expander = SExprExpander(context)
        val view = context.createControl(expander)
        context[editorParentRegion] = view
        val eval = Button("Evaluate").apply {
            setOnAction {
                val result = expander.result.now.orNull() ?: return@setOnAction
                val msg = try {
                    result.evaluate().toString()
                } catch (e: LispRuntimeError) {
                    e.message
                }
                Alert(
                    INFORMATION,
                    msg
                ).show()
            }
        }
        return VBox(eval, view)
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(LispEditorTest::class.java, *args)
        }
    }
}