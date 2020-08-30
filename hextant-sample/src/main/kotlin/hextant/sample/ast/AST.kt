/**
 * @author Nikolaus Knop
 */

@file: Suppress("unused")

package hextant.sample.ast

import hextant.sample.rt.Context

/**
 * An identifier
 * @constructor
 * @param str must be a valid identifier containing only letters
 */
data class Name(val str: String)

/**
 * A Top level object
 */
interface TopLevel

/**
 * An expression of type [T]
 */
interface Expr<out T> {
    /**
     * @return the result of evaluating this expression in the specified [context]
     */
    fun eval(context: Context): T

    /**
     * @return the type of this [Expr]
     */
    val type: Type
}

/**
 * An expression of type [Int]
 */
interface IntExpr : Expr<Int> {
    /**
     * @return [Type.INT]
     */
    override val type: Type
        get() = Type.INT
}

/**
 * An expression of type [Boolean]
 */
interface BooleanExpr : Expr<Boolean> {
    /**
     * @return [Type.BOOLEAN]
     */
    override val type: Type
        get() = Type.BOOLEAN
}

/**
 * A constant expression of type [T]
 * @constructor
 */
sealed class Constant<out T> : Expr<T> {
    /**
     * @return the constant value of this expression
     */
    abstract val value: T

    /**
     * @return the constant [value]
     */
    final override fun eval(context: Context): T = value
}

/**
 * An integer literal with constant [value]
 * @constructor
 */
data class IntLiteral(override val value: Int) : Constant<Int>(), IntExpr

/**
 * A boolean literal with constant [value]
 */
data class BooleanLiteral(override val value: Boolean) : Constant<Boolean>(),
                                                         BooleanExpr

/**
 * An operator combining two integers
 */
enum class IntOperator(private val op: (Int, Int) -> Int) {
    /**
     * The + operator which combines two ints to their sum
     */
    Plus(Int::plus),

    /**
     * The - operator which combines two ints to their difference
     */
    Minus(Int::minus),

    /**
     * The * operator which combines two ints to their product
     */
    Times(Int::times),

    /**
     * The / operator which combines two ints to their quotient
     */
    Div(Int::div);

    /**
     * Combine the two operands [left] and [right]
     */
    fun operate(left: Int, right: Int) = op(left, right)

    companion object {
        val operatorMap = mapOf(
            "+" to Plus,
            "-" to Minus,
            "*" to Times,
            "/" to Div
        )
    }
}

/**
 * An Operator application of type int
 * @constructor
 * @param left the left expression
 * @param op the operator combing both expressions
 * @param right the right expression
 */
data class IntOperatorApplication(val left: IntExpr, val op: IntOperator, val right: IntExpr) :
    IntExpr {
    /**
     * @return the result of applying [op] to the results of evaluating [left] and [right] in the specified [context]
     */
    override fun eval(context: Context): Int = op.operate(left.eval(context), right.eval(context))
}

/**
 * An operator combining two booleans
 */
enum class BooleanOperator(private val op: (Boolean, Boolean) -> Boolean) {
    /**
     * The && operator which only returns `true` if both operands are `true`
     */
    AND(Boolean::and),

    /**
     * The|| operator which returns `true` if at least one of the operands is `true`
     */
    OR(Boolean::or),

    /**
     * The XOR operator which returns `true` if exactly one of the operands is `true`
     */
    XOR(Boolean::xor);

    /**
     * Combine the two operands [left] and [right]
     */
    fun operate(left: Boolean, right: Boolean): Boolean = op(left, right)
}

/**
 * An Operator application of type boolean
 * @constructor
 * @param left the left expression
 * @param op the operator combing both expressions
 * @param right the right expression
 */
data class BooleanOperatorApplication(val left: BooleanExpr, val op: BooleanOperator, val right: BooleanExpr) :
    BooleanExpr {
    override fun eval(context: Context): Boolean = op.operate(left.eval(context), right.eval(context))
}

/**
 * A function call to a function returning an int
 * @property func the definition of the called function
 * @property args the list of arguments passed to the function
 */
data class IntFuncCall(val func: IntFunctionDefinition, val args: List<Expr<Any>>) :
    IntExpr {
    override fun eval(context: Context): Int {
        return context.run {
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

/**
 * A function call to a function returning an boolean
 * @property func the definition of the called function
 * @property args the list of arguments passed to the function
 */
data class BoolFuncCall(val func: BoolFunctionDefinition, val args: List<Expr<Any>>) :
    BooleanExpr {
    override fun eval(context: Context): Boolean = context.run {
        prepareFuncCall(args, func)
        func.body.eval(context)
    }
}

/**
 * A type in the sample language
 */
enum class Type {
    /**
     * The int type
     */
    INT {
        override fun isInstance(value: Any?) = value is Int
    },

    /**
     * The boolean type
     */
    BOOLEAN {
        override fun isInstance(value: Any?) = value is Boolean
    };

    /**
     * @return `true` if and only if [value] is an instance of this [Type]
     */
    abstract fun isInstance(value: Any?): Boolean
}

/**
 * A parameter of a function
 */
data class Parameter(val name: Name, val type: Type)

/**
 * A function definition
 */
sealed class FunctionDefinition : Named, TopLevel {
    /**
     * @return the name of this function
     */
    abstract override val name: Name

    /**
     * @return the declared parameters of this function
     */
    abstract val parameters: List<Parameter>
}

/**
 * A named element
 */
interface Named {
    /**
     * @return the name of this element
     */
    val name: Name
}

/**
 * A function definition with return type int
 * @constructor
 * @param name the name of this function
 * @param parameters the declared parameters of this function
 * @param body the function body which is evaluated when calling this function
 */
data class IntFunctionDefinition(
    override val name: Name,
    override val parameters: List<Parameter>,
    val body: IntExpr
) : FunctionDefinition()

/**
 * A function definition with return type boolean
 * @constructor
 * @param name the name of this function
 * @param parameters the declared parameters of this function
 * @param body the function body which is evaluated when calling this function
 */
data class BoolFunctionDefinition(
    override val name: Name,
    override val parameters: List<Parameter>,
    val body: BooleanExpr
) : FunctionDefinition()

/**
 * An If-expr
 * @property cond the condition which is evaluated to decide which side to evaluate
 * @property then the expr which is evaluated when [cond] evaluates to `true`
 * @property otherwise the expr which is evaluated when [cond] evaluates to `false`
 */
sealed class IfExpr<T, E : Expr<T>>(val cond: BooleanExpr, val then: E, val otherwise: E) :
    Expr<T> {
    override fun eval(context: Context): T =
        if (cond.eval(context)) then.eval(context)
        else otherwise.eval(context)

    override fun toString(): String = buildString {
        append("if ")
        append(cond)
        append(" then ")
        appendLine(then)
        append(" else ")
        append(otherwise)
    }
}

/**
 * An if expr of type int
 * @param cond the condition which is evaluated to decide which side to evaluate
 * @param then the expr which is evaluated when [cond] evaluates to `true`
 * @param otherwise the expr which is evaluated when [cond] evaluates to `false`
 */
class IntIfExpr(
    cond: BooleanExpr,
    then: IntExpr,
    otherwise: IntExpr
) : IfExpr<Int, IntExpr>(cond, then, otherwise), IntExpr {
    override val type: Type
        get() = Type.INT
}

/**
 * An if expr of type boolean
 * @param cond the condition which is evaluated to decide which side to evaluate
 * @param then the expr which is evaluated when [cond] evaluates to `true`
 * @param otherwise the expr which is evaluated when [cond] evaluates to `false`
 */
class BooleanIfExpr(
    cond: BooleanExpr,
    then: BooleanExpr,
    otherwise: BooleanExpr
) : IfExpr<Boolean, BooleanExpr>(cond, then, otherwise),
    BooleanExpr {
    override val type: Type
        get() = Type.BOOLEAN
}

/**
 * An expression which when evaluated tries to get the value of the boolean variable with the specified [name]
 */
data class GetBoolean(val name: Name) : BooleanExpr {
    override fun eval(context: Context): Boolean = context.getBoolean(name)
}

/**
 * An expression which when evaluated tries to get the value of the int variable with the specified [name]
 */
data class GetInt(val name: Name) : IntExpr {
    override fun eval(context: Context): Int = context.getInt(name)
}