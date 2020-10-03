/**
 * @author Nikolaus Knop
 */

package hextant.lispy.parser

import hextant.lispy.rt.Env
import hextant.lispy.rt.evaluate

fun main() {
    val str = "(let square (lambda x (* x x)) (+ (square 8) (square 7)))"
    val input = CharInput(str)
    val tokens = lex(input)
    println("tokens = $tokens")
    val expr = parseExpr(tokens)
    println("expr = $expr")
    val result = expr.evaluate(Env.root())
    println("result = $result")
}