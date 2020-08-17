/**
 * @author Nikolaus Knop
 */

package hextant.lisp.parser

import hextant.lisp.FileScope

fun main() {
    val str = "(let square (lambda x (* x x)) (+ (square 8) (square 7)))"
    val input = CharInput(str)
    val tokens = lex(input)
    println("tokens = $tokens")
    val expr = parseExpr(tokens, FileScope.empty)
    println("expr = $expr")
    val result = expr.evaluate()
    println("result = $result")
}