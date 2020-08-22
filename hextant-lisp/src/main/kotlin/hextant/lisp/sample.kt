/**
 * @author Nikolaus Knop
 */

package hextant.lisp

import bundles.SimpleProperty
import hextant.context.Context
import hextant.context.createView
import hextant.lisp.editor.LispProperties
import hextant.lisp.editor.SExprExpander
import hextant.main.HextantApplication
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.control.Button
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import reaktive.value.now
import validated.orNull

val scope = FileScope.empty

////(if (+ 4 -4) (- 4 7) (* (let (x 2.5) (y 7) (/ y x)) 23))
//val expr = IfExpr(
//    cond = Apply(
//        GetVal("+", scope), listOf(
//            IntLiteral(4),
//            IntLiteral(-4)
//        )
//    ),
//    then = Apply(
//        GetVal("-", scope), listOf(
//            IntLiteral(4),
//            IntLiteral(7)
//        )
//    ),
//    otherwise = Apply(
//        GetVal("*", scope), listOf(
//            let(
//                listOf(
//                    "x" to DoubleLiteral(2.5),
//                    "y" to IntLiteral(7)
//                ),
//                Apply(
//                    GetVal("/", scope),
//                    listOf(
//                        GetVal("y", scope),
//                        GetVal("x", scope)
//                    )
//                )
//            ),
//            IntLiteral(23)
//        )
//    )
//)

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
        val view = context.createView(expander)
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
        /**
         * The parent region of the visual editor
         */
        val editorParentRegion =
            SimpleProperty<Region>("editor parent region")

        @JvmStatic
        fun main(args: Array<String>) {
            launch(LispEditorTest::class.java, *args)
        }
    }
}