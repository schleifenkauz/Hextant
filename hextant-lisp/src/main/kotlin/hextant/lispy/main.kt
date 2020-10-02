/**
 *@author Nikolaus Knop
 */

package hextant.lispy

fun main() {
    debugExpr()
}

private fun debugExpr() {
    val e = call("dbg".s, call("+".s, lit(1), lit(2)))
    evaluate(e, Env.root())
}

private fun printSquares() {
    val lst = quote(call(lit(1), lit(2), lit(3), lit(4)))
    val square = call("lambda".s, call("x".s), call("*".s, "x".s, "x".s))
    val e = call("map".s, square, lst)
    println(e)
    println(evaluate(e, Env.root()))
}