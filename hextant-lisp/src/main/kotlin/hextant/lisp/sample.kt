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