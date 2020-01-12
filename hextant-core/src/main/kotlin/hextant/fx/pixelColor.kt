/**
 * @author Nikolaus Knop
 */

package hextant.fx

import java.awt.*
import java.util.*

fun main() {
    val sc = Scanner(System.`in`)
    println("Ask your question!")
    while (true) {
        val question = sc.next()
        Thread.sleep(10000)
        val color = getColorAtMousePointer()
        val answer = when (question) {
            "red"   -> color.red.toString()
            "green" -> color.green.toString()
            "blue"  -> color.blue.toString()
            "alpha" -> color.alpha.toString()
            "rgb"   -> "${color.red}, ${color.green}, ${color.blue}"
            "help"  -> "Possible question: red, green, blue, alpha"
            "quit"  -> return
            else    -> "Invalid command, type 'help' for help"
        }
        println(answer)
    }
}

private val robot = Robot()

private fun getColorAtMousePointer(): Color {
    val loc = MouseInfo.getPointerInfo().location
    return robot.getPixelColor(loc.x, loc.y)
}