package hextant.sample.rt

import hextant.sample.*
import hextant.sample.ControlFlowStatement.Type.Break
import hextant.sample.ControlFlowStatement.Type.Continue

class Interpreter(definitions: List<FunctionDefinition>) {
    private val functionTable = definitions.associateBy { it.name }

    private fun evaluate(expr: Expr, ctx: RuntimeContext): Any = when (expr) {
        is Reference -> ctx.get(expr.name)
        is IntLiteral -> expr.value
        is BooleanLiteral -> expr.value
        is BinaryExpr -> expr.operator.apply(evaluate(expr.lhs, ctx), evaluate(expr.rhs, ctx))
        is FunctionCall -> {
            val def = functionTable.getOrElse(expr.name) { error("Cannot resolve function '${expr.name}'") }
            check(def.parameters.size == expr.arguments.size) { "Illegal count of arguments" }
            val context = RuntimeContext.root()
            for ((param, arg) in def.parameters.zip(expr.arguments)) {
                context.define(param.name, evaluate(arg, ctx))
            }
            try {
                execute(def.body, context)
            } catch (ex: ReturnException) {
                ex.value
            }
        }
    }

    fun execute(statement: Statement, ctx: RuntimeContext) {
        when (statement) {
            is PrintStatement -> println(evaluate(statement.expr, ctx))
            is ExprStatement -> evaluate(statement.expr, ctx)
            is Definition -> ctx.define(statement.name, evaluate(statement.value, ctx))
            is Assignment -> ctx.assign(statement.name, evaluate(statement.value, ctx))
            is AugmentedAssignment -> {
                val e = BinaryExpr(Reference(statement.name), statement.operator, statement.value)
                execute(Assignment(statement.name, e), ctx)
            }
            is Block -> {
                val context = ctx.child()
                for (st in statement.statements) {
                    execute(st, context)
                }
            }
            is ControlFlowStatement -> when (statement.type) {
                Break    -> throw BreakException()
                Continue -> throw ContinueException()
            }
            is ReturnStatement -> throw ReturnException(evaluate(statement.expr, ctx))
            is IfStatement -> if (evaluate(statement.condition, ctx) == true) execute(
                statement.consequence,
                ctx.child()
            )
            is WhileLoop -> {
                val context = ctx.child()
                while (evaluate(statement.condition, ctx) == true) {
                    execute(statement.body, context)
                }
            }
            is ForLoop -> {
                val context = ctx.child()
                execute(statement.initializer, context)
                while (evaluate(statement.condition, context) == true) {
                    execute(statement.body, context)
                    execute(statement.after, context)
                }
            }
        }
    }

    private class ReturnException(val value: Any) : Exception()
    private class BreakException : Exception()
    private class ContinueException : Exception()
}