/**
 *@author Nikolaus Knop
 */

package hextant.lispy.parser

import hextant.lispy.parser.CharInput.Cont.*
import java.io.Reader
import java.io.StringReader

class CharInput(private val reader: Reader) {
    constructor(string: String) : this(StringReader(string))

    fun next(): Int {
        val res = current
        current = reader.read()
        return res
    }

    fun peek(): Int = current

    private var current = reader.read()

    inline fun forEach(consume: (Char) -> Cont) {
        while (true) {
            val i = peek()
            if (i == -1) return
            val c = i.toChar()
            when (consume(c)) {
                Consume -> next()
                DoNotConsume -> {
                }
                TerminateAndConsume -> {
                    next()
                    return
                }
                Terminate -> return
            }
        }
    }

    enum class Cont {
        Consume, DoNotConsume, TerminateAndConsume, Terminate
    }
}