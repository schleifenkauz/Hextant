/**
 * @author Nikolaus Knop
 */

package hextant.sample

import hextant.codegen.*
import hextant.core.editor.TokenType
import hextant.sample.editor.*
import validated.*

@Token
data class Identifier(private val str: String) {
    override fun toString(): String = str

    companion object : TokenType<Identifier> {
        override fun compile(token: String): Validated<Identifier> = token
            .takeIf { it.none { c -> c.isWhitespace() } }
            .validated { invalid("Invalid identifier '$token'") }
            .map(::Identifier)
    }
}

@Alternative
@Expandable(ExprExpanderDelegator::class, subtypeOf = Expr::class)
@EditableList
sealed class Expr

@Token(subtypeOf = Expr::class)
data class Reference(val name: Identifier) : Expr() {
    override fun toString(): String = "$name"

    companion object : TokenType<Reference> {
        override fun compile(token: String): Validated<Reference> = Identifier.compile(token).map(::Reference)
    }
}

@Token(subtypeOf = Expr::class)
data class IntLiteral(val value: Int) : Expr() {
    override fun toString(): String = "$value"

    companion object : TokenType<IntLiteral> {
        override fun compile(token: String): Validated<IntLiteral> = token
            .toIntOrNull()
            .validated { invalid("Invalid integer literal '$token'") }
            .map(::IntLiteral)
    }
}

@Token(subtypeOf = Expr::class)
data class BooleanLiteral(val value: Boolean) : Expr() {
    override fun toString(): String = "$value"

    companion object : TokenType<BooleanLiteral> {
        override fun compile(token: String): Validated<BooleanLiteral> = when (token) {
            "false" -> valid(BooleanLiteral(false))
            "true" -> valid(BooleanLiteral(true))
            else    -> invalid("Invalid boolean literal '$token'")
        }
    }
}

@Token
enum class BinaryOperator(private val str: String) {
    Plus("+") {
        override fun apply(lhs: Any, rhs: Any): Any = lhs as Int + rhs as Int
    },
    Minus("-") {
        override fun apply(lhs: Any, rhs: Any): Any = lhs as Int - rhs as Int
    },
    Times("*") {
        override fun apply(lhs: Any, rhs: Any): Any = lhs as Int * rhs as Int
    },
    Div("/") {
        override fun apply(lhs: Any, rhs: Any): Any = lhs as Int / rhs as Int
    },
    Less("<") {
        override fun apply(lhs: Any, rhs: Any): Any = (lhs as Int) < (rhs as Int)
    },
    LessOrEqual("<=") {
        override fun apply(lhs: Any, rhs: Any): Any = (lhs as Int) <= (rhs as Int)
    },
    Greater(">") {
        override fun apply(lhs: Any, rhs: Any): Any = (lhs as Int) > (rhs as Int)
    },
    GreaterOrEqual(">=") {
        override fun apply(lhs: Any, rhs: Any): Any = (lhs as Int) >= (rhs as Int)
    },
    Equals("==") {
        override fun apply(lhs: Any, rhs: Any): Any = lhs == rhs
    },
    NotEquals("!=") {
        override fun apply(lhs: Any, rhs: Any): Any = lhs != rhs
    },
    And("&&") {
        override fun apply(lhs: Any, rhs: Any): Any = lhs as Boolean && rhs as Boolean
    },
    Or("||") {
        override fun apply(lhs: Any, rhs: Any): Any = lhs as Boolean || rhs as Boolean
    },
    Xor("^^") {
        override fun apply(lhs: Any, rhs: Any): Any = lhs as Boolean xor rhs as Boolean
    };

    abstract fun apply(lhs: Any, rhs: Any): Any

    override fun toString(): String = str

    companion object : TokenType<BinaryOperator> {
        private val byToken = values().associateBy { it.str }

        override fun compile(token: String): Validated<BinaryOperator> =
            byToken[token].validated { invalid("No such binary operator '$token'") }
    }
}

@Compound(subtypeOf = Expr::class)
data class BinaryExpr(val lhs: Expr, val operator: BinaryOperator, val rhs: Expr) : Expr() {
    override fun toString(): String = "$lhs $operator $rhs"
}

@Compound(subtypeOf = Expr::class)
data class FunctionCall(val name: Identifier, val arguments: List<Expr>) : Expr() {
    override fun toString(): String = "$name(${arguments.joinToString(", ")})"
}

@Alternative
@Expandable(StatementExpanderDelegator::class, subtypeOf = Statement::class)
@EditableList
sealed class Statement

@Compound(subtypeOf = Statement::class)
data class PrintStatement(val expr: Expr) : Statement() {
    override fun toString(): String = "print $expr"
}

@Compound(subtypeOf = Statement::class)
data class ExprStatement(val expr: Expr) : Statement() {
    override fun toString(): String = "$expr"
}

@Compound(subtypeOf = Statement::class)
data class Definition(val type: SimpleType, val name: Identifier, val value: Expr) : Statement() {
    override fun toString(): String = "let $name = $value"
}

@Compound(subtypeOf = Statement::class)
data class Assignment(val name: Identifier, val value: Expr) : Statement() {
    override fun toString(): String = "$name = $value"
}

@Compound(subtypeOf = Statement::class)
data class AugmentedAssignment(val name: Identifier, val operator: BinaryOperator, val value: Expr) : Statement() {
    override fun toString(): String = "$name $operator= $value"
}

@UseEditor(BlockEditor::class)
data class Block(val statements: List<Statement>) : Statement() {
    override fun toString(): String = buildString {
        appendLine("{")
        for (st in statements) appendLine(st)
        append("}")
    }
}

@Token(subtypeOf = Statement::class)
data class ControlFlowStatement(val type: Type) : Statement() {
    enum class Type(private val str: String) {
        Break("break"), Continue("continue");

        override fun toString(): String = str
    }

    companion object : TokenType<ControlFlowStatement> {
        private val byToken = Type.values().associateBy { it.toString() }

        override fun compile(token: String): Validated<ControlFlowStatement> = byToken[token]
            .validated { invalid("No such binary operator '$token'") }
            .map(::ControlFlowStatement)
    }
}

@Compound(subtypeOf = Statement::class)
data class ReturnStatement(val expr: Expr) : Statement() {
    override fun toString(): String = "return $expr"
}

@Compound(subtypeOf = Statement::class)
data class IfStatement(
    val condition: Expr,
    val consequence: Block
) : Statement() {
    override fun toString(): String = "if $condition then $consequence"
}

@Compound(subtypeOf = Statement::class)
data class WhileLoop(
    val condition: Expr,
    val body: Block
) : Statement() {
    override fun toString(): String = "while($condition) $body"
}

@UseEditor(ForLoopEditor::class)
data class ForLoop(
    val initializer: Statement,
    val condition: Expr,
    val after: Statement,
    val body: Block
) : Statement() {
    override fun toString(): String = "for($initializer;$condition;$after) $body"
}

@Token
enum class SimpleType(private val str: String) {
    Integer("int"), Bool("bool"), Void("void");

    override fun toString(): String = str

    companion object : TokenType<SimpleType> {
        private val byToken = values().associateBy { it.str }

        override fun compile(token: String): Validated<SimpleType> =
            byToken[token].validated { invalid("No such type '$token'") }
    }
}

@Compound
@EditableList
data class Parameter(val type: SimpleType, val name: Identifier) {
    override fun toString(): String = "$type $name"
}

@UseEditor(FunctionDefinitionEditor::class)
@EditableList
data class FunctionDefinition(
    val returnType: SimpleType,
    val name: Identifier,
    val parameters: List<Parameter>,
    val body: Block
)

data class Program(val functions: List<FunctionDefinition>, val main: Block)