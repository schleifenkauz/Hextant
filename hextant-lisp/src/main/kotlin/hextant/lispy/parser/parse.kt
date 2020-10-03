/**
 * @author Nikolaus Knop
 */

package hextant.lispy.parser

import hextant.lispy.*
import java.io.Reader
import java.text.ParseException
import java.util.*

fun parseExpr(
    tokens: LinkedList<Token>
): SExpr {
    return when (val next = tokens.poll()) {
        Token.OpeningParen -> return parseApply(tokens)
        Token.ClosingParen -> throw ParseException("Unmatched closing bracket", -1)
        is Token.Identifier -> Symbol(next.name)
        is Token.CharLiteral -> TODO("character literals are not implemented")
        is Token.IntLiteral -> IntLiteral(next.value)
        is Token.DoubleLiteral -> TODO("double literals are not implemented")
        is Token.StringLiteral -> TODO("string literals are not implemented")
        is Token.Quote -> Quoted(parseExpr(tokens))
    }
}

private fun parseApply(
    tokens: LinkedList<Token>
): SExpr =
    if (tokens.peek() == Token.ClosingParen) {
        tokens.poll()
        nil
    } else Pair(parseExpr(tokens), parseApply(tokens))

fun parseExpr(input: Reader): SExpr {
    val toks = lex(CharInput(input))
    return parseExpr(toks)
}

