/**
 * @author Nikolaus Knop
 */

package hextant.lisp

import hextant.lisp.SinglyLinkedList.Empty

typealias Identifier = String

sealed class SExpr {
    abstract val children: Iterable<SExpr>

    abstract fun evaluate(): Value

    abstract fun replaceOccurrencesOf(identifier: Identifier, replacement: SExpr): SExpr
}

data class GetVal(val name: Identifier, val scope: FileScope) : SExpr() {
    override val children: Iterable<SExpr>
        get() = emptySet()

    override fun evaluate(): Value = scope.lookup(name)

    override fun replaceOccurrencesOf(identifier: Identifier, replacement: SExpr) =
        if (name == identifier) replacement
        else this

    override fun toString(): String = name
}

data class Apply(val expressions: SinglyLinkedList<SExpr>) : SExpr() {
    override val children: Iterable<SExpr>
        get() = mutableSetOf<SExpr>().apply {
            addAll(expressions)
        }

    override fun evaluate(): Value =
        if (expressions is Empty) throw LispRuntimeError("Cannot evaluate empty list") else {
            expressions.head.evaluate().apply(expressions.tail.toList())
        }

    override fun replaceOccurrencesOf(identifier: Identifier, replacement: SExpr): SExpr =
        Apply(expressions.map { it.replaceOccurrencesOf(identifier, replacement) })

    override fun toString(): String = buildString {
        append('(')
        expressions.joinTo(this, separator = "")
        append(')')
    }
}

sealed class ScalarExpr : SExpr() {
    final override val children: Iterable<SExpr>
        get() = emptySet()

    final override fun replaceOccurrencesOf(identifier: Identifier, replacement: SExpr): SExpr = this
}

data class IntLiteral(val value: Int) : ScalarExpr() {
    override fun evaluate(): Value = IntegerValue(value)
}

data class DoubleLiteral(val value: Double) : ScalarExpr() {
    override fun evaluate(): Value = DoubleValue(value)
}

data class StringLiteral(val value: String) : ScalarExpr() {
    override fun evaluate(): Value = StringValue(value)
}

data class CharLiteral(val value: Char) : ScalarExpr() {
    override fun evaluate(): Value = CharValue(value)
}

data class LazyExpr(val expr: SExpr) : SExpr() {
    override val children: Iterable<SExpr>
        get() = expr.children + expr

    private val cached by lazy { expr.evaluate() }

    override fun evaluate(): Value = cached

    override fun replaceOccurrencesOf(identifier: Identifier, replacement: SExpr): SExpr =
        LazyExpr(expr.replaceOccurrencesOf(identifier, replacement))
}

typealias Program = Map<Identifier, Value>