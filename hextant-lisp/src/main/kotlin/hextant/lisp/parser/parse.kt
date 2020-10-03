/**
 * @author Nikolaus Knop
 */

package hextant.lisp.parser

import hextant.lisp.*
import java.text.ParseException
import java.util.*

fun parseExpr(
    tokens: LinkedList<Token>
): SExpr = when (val next = tokens.poll()) {
    Token.OpeningParen -> parseList(tokens)
    Token.ClosingParen -> throw ParseException("Unmatched closing bracket", -1)
    is Token.Identifier -> Symbol(next.name)
    is Token.CharLiteral -> TODO("character literals are not implemented")
    is Token.IntLiteral -> IntLiteral(next.value)
    is Token.DoubleLiteral -> TODO("double literals are not implemented")
    is Token.StringLiteral -> TODO("string literals are not implemented")
    is Token.Quote -> Quotation(parseExpr(tokens))
    is Token.BackQuote -> QuasiQuotation(parseExpr(tokens))
    is Token.Comma -> Unquote(parseExpr(tokens))
    null -> throw ParseException("Unexpected end of input", -1)
}


private fun parseList(tokens: LinkedList<Token>): SExpr =
    if (tokens.peek() == Token.ClosingParen) {
        tokens.poll()
        nil
    } else Pair(parseExpr(tokens), parseList(tokens))


