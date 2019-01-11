/**
 * @author Nikolaus Knop
 */

package hextant.lisp.parser

import hextant.lisp.FileScope

fun main(args: Array<String>) {
    val str = "(+ (* 1 2) (/ 6 2))"
    val input = CharInput(str)
    val tokens = lex(input)
    println("tokens = $tokens")
    val expr = parseExpr(tokens, FileScope.empty)
    println("expr = $expr")
    val result = expr.evaluate()
    println("result = $result")
}