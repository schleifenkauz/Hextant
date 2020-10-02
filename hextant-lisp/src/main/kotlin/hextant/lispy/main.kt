/**
 *@author Nikolaus Knop
 */

package hextant.lispy

fun main() {
    val lst = quote(call(lit(1), lit(2), lit(3), lit(4)))
    println(lst)
    val square = call("lambda".s, call("x".s), call("*".s, "x".s, "x".s))
    val e = call("map".s, square, lst)
    println(e)
    println(evaluate(e))
}