/**
 * @author Nikolaus Knop
 */

package hextant.lisp.parser

import hextant.lisp.SExpr
import hextant.lisp.rt.RuntimeScope
import hextant.lisp.rt.evaluate
import java.io.File
import java.io.Reader
import java.io.StringReader
import java.net.URL

fun parseExpr(input: Reader): SExpr = parseExpr(lex(CharInput(input)))

fun parseExpr(input: String): SExpr = parseExpr(StringReader(input))

fun evaluate(input: Reader, scope: RuntimeScope = RuntimeScope.root()): SExpr = parseExpr(input).evaluate(scope)

fun evaluate(str: String, scope: RuntimeScope = RuntimeScope.root()): SExpr = evaluate(StringReader(str), scope)

fun loadFile(reader: Reader, scope: RuntimeScope) {
    val tokens = lex(CharInput(reader))
    while (tokens.isNotEmpty()) {
        val e = parseExpr(tokens)
        e.evaluate(scope)
    }
}

fun loadFile(url: URL, scope: RuntimeScope) {
    url.openStream().bufferedReader().use { r ->
        loadFile(r, scope)
    }
}

fun loadFile(file: File, scope: RuntimeScope = RuntimeScope.root()) {
    file.bufferedReader().use { r ->
        loadFile(r, scope)
    }
}