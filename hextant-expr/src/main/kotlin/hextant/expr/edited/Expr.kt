package hextant.expr.edited

interface Expr {
    val value: Int
}

data class IntLiteral(override val value: Int) : Expr {
    override fun toString(): String = value.toString()
}

data class OperatorApplication(val op1: Expr, val op2: Expr, val operator: Operator) :
    Expr {
    override val value: Int
        get() = operator.apply(op1.value, op2.value)

    override fun toString(): String = "$op1 ${operator.name} $op2"
}

sealed class Operator(private val operation: (Int, Int) -> Int, val name: String, val isCommutative: Boolean) {
    fun apply(op1: Int, op2: Int) = operation(op1, op2)

    object Plus : Operator(Int::plus, "+", isCommutative = true)
    object Minus : Operator(Int::minus, "-", isCommutative = false)
    object Times : Operator(Int::times, "*", isCommutative = true)
    object Div : Operator(Int::div, "/", isCommutative = false)

    override fun toString(): String = name

    companion object {
        fun of(text: String) = map[text] ?: throw NoSuchElementException("No such operator '$text'")

        fun isValid(text: String) = map.containsKey(text)

        private val map by lazy {
            mapOf(
                "+" to Plus,
                "-" to Minus,
                "*" to Times,
                "/" to Div
            )
        }
    }
}

data class Sum(val expressions: List<Expr>) : Expr {
    override val value: Int
        get() = expressions.sumBy { it.value }

    override fun toString(): String = buildString {
        append("sum ")
        expressions.joinTo(this, " + ")
    }
}