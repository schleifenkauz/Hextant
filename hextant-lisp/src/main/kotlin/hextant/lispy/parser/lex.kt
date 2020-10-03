/**
 * @author Nikolaus Knop
 */

package hextant.lispy.parser

import hextant.lispy.parser.CharInput.Cont.*
import hextant.lispy.parser.Token.*
import java.text.ParseException
import java.util.*

sealed class Token {
    object OpeningParen : Token() {
        override fun toString(): String = "("
    }

    object ClosingParen : Token() {
        override fun toString(): String = ")"
    }

    object Quote : Token() {
        override fun toString(): String = "'"
    }

    object BackQuote : Token() {
        override fun toString(): String = "`"
    }

    object Comma : Token() {
        override fun toString(): String = ","
    }

    data class Identifier(val name: String) : Token() {
        override fun toString(): String = name
    }

    data class CharLiteral(val value: Char) : Token() {
        override fun toString(): String = "'$value'"
    }

    data class IntLiteral(val value: Int) : Token() {
        override fun toString(): String = value.toString()
    }

    data class DoubleLiteral(val value: Double) : Token() {
        override fun toString(): String = value.toString()
    }

    data class StringLiteral(val value: String) : Token() {
        override fun toString(): String = "\"$value\""
    }
}

fun lex(input: CharInput): LinkedList<Token> {
    val res = LinkedList<Token>()
    input.forEach { c ->
        val tok = when {
            c == '('         -> OpeningParen
            c == ')'         -> ClosingParen
            c == '"'         -> parseStringLiteral(input, LinkedList())
            c == '\''        -> Quote
            c == '`'         -> BackQuote
            c == ','         -> Comma
            c.isDigit()      -> {
                input.next()
                val num = parseNumber(input, c.digit())
                res.add(num)
                return@forEach DoNotConsume
            }
            c.isWhitespace() -> return@forEach Consume
            else             -> {
                val id = parseIdentifier(input)
                res.add(id)
                return@forEach DoNotConsume
            }
        }
        res.add(tok)
        Consume
    }
    return res
}

private fun parseIdentifier(input: CharInput): Identifier {
    val acc = LinkedList<Char>()
    input.forEach { c ->
        if (c in terminators) Terminate
        else {
            acc.add(c)
            Consume
        }
    }
    val name = acc.joinToString(separator = "")
    return Identifier(name)
}

private fun parseNumber(input: CharInput, acc: Int): Token {
    var accumulator = acc
    input.forEach { c ->
        when {
            c == '.'         -> {
                input.next()
                return parseDoubleAfterComma(input, acc.toDouble())
            }
            c.isDigit()      -> {
                accumulator = accumulator * 10 + c.digit()
                Consume
            }
            c in terminators -> Terminate
            else             -> throw ParseException("Unexpected character \"$c\" after int literal", -1)
        }
    }
    return IntLiteral(accumulator)
}

private fun parseDoubleAfterComma(input: CharInput, acc: Double): DoubleLiteral {
    var multiplier = 0.1
    var accumulator = acc
    input.forEach { c ->
        when {
            c in terminators -> Terminate
            c.isDigit()      -> {
                accumulator += c.digit() * multiplier
                multiplier *= 0.1
                Consume
            }
            else             -> throw ParseException("Unexpected character \"$c\" after double literal", -1)
        }
    }
    return DoubleLiteral(accumulator)
}

private fun Char.digit(): Int = this - '0'

private val terminators = setOf('(', ')', '"', '\'', ' ', '\n', '\r', '\t')

private fun parseStringLiteral(
    input: CharInput,
    acc: LinkedList<Char>
): StringLiteral {
    check(input.next().toChar() == '\'')
    while (true) {
        val i = input.peek()
        if (i == -1) break
        val c = i.toChar()
        if (c == '"') {
            input.next()
            break
        } else {
            acc.add(c)
            input.next()
        }
    }
    return StringLiteral(acc.joinToString(separator = ""))
}