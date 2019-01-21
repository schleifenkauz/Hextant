/**
 * @author Nikolaus Knop
 */

package hextant.lisp.parser

import hextant.lisp.*
import hextant.lisp.parser.Token.ClosingParen
import java.io.Reader
import java.io.StringReader
import java.text.ParseException
import java.util.*

fun parseExpr(
    tokens: LinkedList<Token>,
    scope: FileScope
): SExpr {
    val next = tokens.poll()
    return when (next) {
        Token.OpeningParen     -> return parseApply(tokens, scope)
        Token.ClosingParen     -> throw ParseException("Unmatched closing bracket", -1)
        is Token.Identifier    -> GetVal(next.name, scope)
        is Token.CharLiteral   -> CharLiteral(next.value)
        is Token.IntLiteral    -> IntLiteral(next.value)
        is Token.DoubleLiteral -> DoubleLiteral(next.value)
        is Token.StringLiteral -> StringLiteral(next.value)
    }
}

private fun parseApply(
    tokens: LinkedList<Token>,
    scope: FileScope
): Apply {
    val exprs = LinkedList<SExpr>()
    while (true) {
        val t = tokens.peek() ?: throw ParseException("Unmatched opening paren", -1)
        if (t == ClosingParen) {
            tokens.poll()
            break
        }
        exprs.add(parseExpr(tokens, scope))
    }
    return Apply(SinglyLinkedList.fromList(exprs))
}

fun parseExpr(input: Reader, scope: FileScope = FileScope.empty): SExpr {
    val toks = lex(CharInput(input))
    return parseExpr(toks, scope)
}

fun evaluate(input: Reader, scope: FileScope = FileScope.empty) =
    parseExpr(input, scope).evaluate()

fun evaluate(str: String, scope: FileScope = FileScope.empty) =
    evaluate(StringReader(str), scope)