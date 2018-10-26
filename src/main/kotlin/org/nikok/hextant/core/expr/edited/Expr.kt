package org.nikok.hextant.core.expr.edited

interface Expr {
    val value: Int
}

data class IntLiteral(override val value: Int): Expr {
    override fun toString(): String = value.toString()
}

data class OperatorApplication(val op1: Expr, val op2: Expr, val operator: Operator): Expr {
    override val value: Int
        get() = operator.apply(op1.value, op2.value)
}

sealed class Operator(private val operation: (Int, Int) -> Int, val name: String) {
    fun apply(op1: Int, op2: Int) = operation(op1, op2)

    object Plus: Operator(Int::plus, "+")
    object Minus: Operator(Int::minus, "-")
    object Times: Operator(Int::times, "*")
    object Div: Operator(Int::div, "/")

    companion object {
        fun of(text: String) = when(text) {
            "+", "plus" -> Plus
            "-", "minus" -> Minus
            "*", "times" -> Times
            "/", "div" -> Div
            else -> null
        }
    }
}