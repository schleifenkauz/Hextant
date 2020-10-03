/**
 * @author Nikolaus Knop
 */

package hextant.lisp.parser

import hextant.lisp.SExpr
import hextant.lisp.rt.Env
import hextant.lisp.rt.evaluate
import java.io.*
import java.net.URL

fun parseExpr(input: Reader): SExpr = parseExpr(lex(CharInput(input)))

fun parseExpr(input: String): SExpr = parseExpr(StringReader(input))

fun evaluate(input: Reader, env: Env = Env.root()): SExpr = parseExpr(input).evaluate(env)

fun evaluate(str: String, env: Env = Env.root()): SExpr = evaluate(StringReader(str), env)

fun loadFile(reader: Reader, env: Env) {
    val tokens = lex(CharInput(reader))
    while (tokens.isNotEmpty()) {
        val e = parseExpr(tokens)
        e.evaluate(env)
    }
}

fun loadFile(url: URL, env: Env) {
    url.openStream().bufferedReader().use { r ->
        loadFile(r, env)
    }
}

fun loadFile(file: File, env: Env = Env.root()) {
    file.bufferedReader().use { r ->
        loadFile(r, env)
    }
}