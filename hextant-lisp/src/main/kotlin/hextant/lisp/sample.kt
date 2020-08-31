/**
 * @author Nikolaus Knop
 */

package hextant.lisp

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

