/**
 *@author Nikolaus Knop
 */

package hextant.lispy

import hextant.lispy.parser.parseExpr
import hextant.lispy.rt.display
import hextant.lispy.rt.evaluate

fun main() {
    printSquares()
    debugExpr()
}

private fun debugExpr() {
    val e = parseExpr("(dbg (+ 1 2))")
    e.evaluate()
}

private fun printSquares() {
    val lst = quote(list(lit(1), lit(2), lit(3), lit(4)))
    val square = list("lambda".s, list("x".s), list("*".s, "x".s, "x".s))
    val e = list("map".s, square, lst)
    println(display(e))
    println(display(e.evaluate()))
}