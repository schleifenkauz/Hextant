/**
 * @author Nikolaus Knop
 */

package hextant.lisp.parser

import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val path = Paths.get(args[0])
    val input = Files.newBufferedReader(path)
    val res = evaluate(input)
    println(res)
}