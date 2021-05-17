/**
 * @author Nikolaus Knop
 */

package hextant.lisp.parser

import hextant.lisp.rt.LispRuntimeException
import hextant.lisp.rt.RuntimeScope
import hextant.lisp.rt.display
import hextant.lisp.rt.evaluate
import java.text.ParseException

fun main() {
    val env = RuntimeScope.root()
    while (true) {
        print("> ")
        val input = readLine() ?: break
        if (input.isBlank()) continue
        val expr = try {
            parseExpr(input)
        } catch (e: ParseException) {
            System.err.println(e.message)
            continue
        } catch (e: Throwable) {
            e.printStackTrace()
            continue
        }
        val value = try {
            expr.evaluate(env)
        } catch (e: LispRuntimeException) {
            System.err.println(e.message)
            continue
        } catch (e: Throwable) {
            e.printStackTrace()
            continue
        }
        println(display(value))
    }
}