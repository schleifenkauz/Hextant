/**
 * @author Nikolaus Knop
 */

package hextant.blocky

import hextant.blocky.BinaryOperator.*
import hextant.blocky.UnaryOperator.Not

private fun eval(expr: Expr, context: Map<String, Int>): Int = when (expr) {
    is IntLiteral       -> expr.value
    is Ref              -> context.getValue(expr.id.toString())
    is BinaryExpression -> {
        val l = eval(expr.left, context)
        val r = eval(expr.right, context)
        when (expr.op) {
            Plus  -> l + r
            Minus -> l - r
            Times -> l * r
            Div   -> l / r
            Mod   -> l % r
            And   -> l and r
            Or    -> l or r
            Xor   -> l xor r
            EQ    -> (l == r).toInt()
            NEQ   -> (l != r).toInt()
            LE    -> (l <= r).toInt()
            GE    -> (l >= r).toInt()
            LO    -> (l < r).toInt()
            GR    -> (l > r).toInt()
        }
    }
    is UnaryExpression  -> {
        val v = eval(expr.operand, context)
        when (expr.op) {
            Not                 -> v.inv()
            UnaryOperator.Minus -> -v
        }
    }
}

private fun Boolean.toInt(): Int = if (this) 1 else 0

private fun execute(statement: Statement, context: MutableMap<String, Int>) {
    when (statement) {
        is Assign -> context[statement.name.toString()] = eval(statement.value, context)
        is Swap   -> {
            val l = statement.left.toString()
            val r = statement.right.toString()
            val t = context[l]!!
            context[l] = context[r]!!
            context[r] = t
        }
        is Print  -> println(eval(statement.expr, context))
    }
}

private fun execute(executable: Executable, context: MutableMap<String, Int>) {
    when (executable) {
        is Block  -> {
            executable.statements.forEach { execute(it, context) }
            execute(executable.next, context)
        }
        is Branch -> {
            val cond = eval(executable.condition, context)
            if (cond != 0) execute(executable.yes, context)
            else execute(executable.no, context)
        }
        is Entry  -> execute(executable.next, context)
        is End    -> return
    }
}

fun execute(program: Program) {
    execute(program.start, mutableMapOf())
}