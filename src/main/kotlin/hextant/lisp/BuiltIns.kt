package hextant.lisp

object BuiltIns {
    private val let = BuiltInFunction("let", arity = 3) { (name, value, body) ->
        Lambda(listOf(name.toString()), ConstantExpr(body)).apply(listOf(ConstantExpr(value)))
    }

    private val plus = BuiltInFunction("+", arity = 2) { (op1, op2) ->
        val operand1 = op1.jvm
        val operand2 = op2.jvm
        when {
            operand1 is Double && operand2 is Double -> DoubleValue(operand1 + operand2)
            operand1 is Double && operand2 is Int    -> DoubleValue(operand1 + operand2)
            operand1 is Int && operand2 is Double    -> DoubleValue(operand1 + operand2)
            operand1 is Int && operand2 is Int       -> IntegerValue(operand1 + operand2)
            operand1 is String && operand2 is String -> StringValue(operand1 + operand2)
            else                                     -> throw LispRuntimeError("Cannot add $operand1 and $operand2")
        }
    }

    private val minus = BuiltInFunction("-", arity = 2) { (op1, op2) ->
        val operand1 = op1.jvm
        val operand2 = op2.jvm
        when {
            operand1 is Double && operand2 is Double -> DoubleValue(operand1 - operand2)
            operand1 is Double && operand2 is Int    -> DoubleValue(operand1 - operand2)
            operand1 is Int && operand2 is Double    -> DoubleValue(operand1 - operand2)
            operand1 is Int && operand2 is Int       -> IntegerValue(operand1 - operand2)
            else                                     -> throw LispRuntimeError("Cannot add $operand1 and $operand2")
        }
    }

    private val times = BuiltInFunction("*", arity = 2) { (op1, op2) ->
        val operand1 = op1.jvm
        val operand2 = op2.jvm
        when {
            operand1 is Double && operand2 is Double -> DoubleValue(operand1 * operand2)
            operand1 is Double && operand2 is Int    -> DoubleValue(operand1 * operand2)
            operand1 is Int && operand2 is Double    -> DoubleValue(operand1 * operand2)
            operand1 is Int && operand2 is Int       -> IntegerValue(operand1 * operand2)
            else                                     -> throw LispRuntimeError("Cannot add $operand1 and $operand2")
        }
    }

    private val div = BuiltInFunction("/", arity = 2) { (op1, op2) ->
        val operand1 = op1.jvm
        val operand2 = op2.jvm
        when {
            operand1 is Double && operand2 is Double -> DoubleValue(operand1 / operand2)
            operand1 is Double && operand2 is Int    -> DoubleValue(operand1 / operand2)
            operand1 is Int && operand2 is Double    -> DoubleValue(operand1 / operand2)
            operand1 is Int && operand2 is Int       -> IntegerValue(operand1 / operand2)
            else                                     -> throw LispRuntimeError("Cannot add $operand1 and $operand2")
        }
    }

    private fun MutableMap<Identifier, BuiltInExpr>.builtIn(func: BuiltInFunction) {
        put(func.name, BuiltInExpr(func))
    }

    private val builtIns = mutableMapOf<Identifier, BuiltInExpr>().apply {
        builtIn(let)
        builtIn(plus)
        builtIn(minus)
        builtIn(times)
        builtIn(div)
    }

    fun get(name: Identifier) = builtIns[name]
}