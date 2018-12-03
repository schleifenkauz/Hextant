/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.sample.ast

import org.nikok.hextant.sample.ast.Type.BOOLEAN
import org.nikok.hextant.sample.ast.Type.INT
import org.nikok.hextant.sample.rt.Context

data class Name(val str: String)

interface TopLevel

interface Expr<out T> {
    fun eval(context: Context): T

    val type: Type
}

interface IntExpr : Expr<Int> {
    override val type: Type
        get() = Type.INT
}

interface BooleanExpr : Expr<Boolean> {
    override val type: Type
        get() = Type.BOOLEAN
}

data class IntLiteral(val value: Int) : IntExpr {
    override fun eval(context: Context): Int = value
}

enum class IntOperator(private val op: (Int, Int) -> Int) {
    Plus(Int::plus),
    Minus(Int::minus),
    Times(Int::times),
    Div(Int::div);

    fun operate(left: Int, right: Int) = op(left, right)
}

data class IntOperatorApplication(val left: IntExpr, val op: IntOperator, val right: IntExpr) : IntExpr {
    override fun eval(context: Context): Int = op.operate(left.eval(context), right.eval(context))
}

enum class BooleanOperator(private val op: (Boolean, Boolean) -> Boolean) {
    AND(Boolean::and),
    OR(Boolean::or),
    XOR(Boolean::xor);

    fun operate(left: Boolean, right: Boolean): Boolean = op(left, right)
}

data class BooleanOperatorApplication(val left: BooleanExpr, val op: BooleanOperator, val right: BooleanExpr) :
    BooleanExpr {
    override fun eval(context: Context): Boolean = op.operate(left.eval(context), right.eval(context))
}

data class IntFuncCall(val func: IntFunctionDefinition, val args: List<Expr<Any>>) : IntExpr {
    override fun eval(context: Context): Int {
        return context.inNewScope {
            prepareFuncCall(args, func)
            func.body.eval(context)
        }
    }
}

private fun Context.prepareFuncCall(
    args: List<Expr<Any>>,
    func: FunctionDefinition
) {
    for ((arg, param) in args.zip(func.parameters)) {
        runtimeCheck(arg.type == param.type) { "Invalid type of $arg for $param" }
        setVar(param.name, arg)
    }
}

data class BoolFuncCall(val func: BoolFunctionDefinition, val args: List<Expr<Any>>) : BooleanExpr {
    override fun eval(context: Context): Boolean = context.inNewScope {
        prepareFuncCall(args, func)
        func.body.eval(context)
    }
}

enum class Type {
    INT {
        override fun isInstance(value: Any?) = value is Int
    },
    BOOLEAN {
        override fun isInstance(value: Any?) = value is Boolean
    };

    abstract fun isInstance(value: Any?): Boolean
}

data class Parameter(val name: Name, val type: Type)

sealed class FunctionDefinition : TopLevel {
    abstract val name: Name

    abstract val parameters: List<Parameter>
}

data class IntFunctionDefinition(
    override val name: Name,
    override val parameters: List<Parameter>,
    val body: IntExpr
) : FunctionDefinition()

data class BoolFunctionDefinition(
    override val name: Name,
    override val parameters: List<Parameter>,
    val body: BooleanExpr
) : FunctionDefinition()

sealed class IfExpr<T, E : Expr<T>>(val cond: BooleanExpr, val then: E, val otherwise: E) : Expr<T> {
    override fun eval(context: Context): T {
        return if (cond.eval(context)) {
            context.inNewScope { then.eval(context) }
        } else {
            context.inNewScope { otherwise.eval(context) }
        }
    }

    override fun toString(): String = buildString {
        append("if ")
        append(cond)
        append(" then ")
        appendln(then)
        append(" else ")
        append(otherwise)
    }
}

class IntIfExpr(
    cond: BooleanExpr,
    then: IntExpr,
    otherwise: IntExpr
) : IfExpr<Int, IntExpr>(cond, then, otherwise), IntExpr {
    override val type: Type
        get() = INT
}

class BooleanIfExpr(
    cond: BooleanExpr,
    then: BooleanExpr,
    otherwise: BooleanExpr
) : IfExpr<Boolean, BooleanExpr>(cond, then, otherwise), BooleanExpr {
    override val type: Type
        get() = BOOLEAN
}

data class GetBoolean(val name: Name) : BooleanExpr {
    override fun eval(context: Context): Boolean = context.getBoolean(name)
}

data class GetInt(val name: Name) : IntExpr {
    override fun eval(context: Context): Int = context.getInt(name)
}