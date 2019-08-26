/**
 * @author Nikolaus Knop
 */

package hextant.blocky

import hextant.*
import hextant.blocky.editor.*
import hextant.codegen.*
import hextant.core.TokenType

@Token
data class Id(private val str: String) {
    override fun toString(): String = str

    companion object : TokenType<Id> {
        override fun compile(token: String): CompileResult<Id> = token
            .takeIf { it.all { c -> c.isLetter() } }
            .okOrErr { "Invalid identifier '$token'" }
            .map(::Id)
    }
}

@Alternative
@Expandable(ExprExpanderDelegator::class, subtypeOf = Expr::class)
sealed class Expr

@Token(subtypeOf = Expr::class)
data class IntLiteral(val value: Int) : Expr() {
    companion object : TokenType<IntLiteral> {
        override fun compile(token: String): CompileResult<IntLiteral> =
            token.toIntOrNull()?.let(::IntLiteral).okOrErr { "Invalid integer literal $token" }
    }
}

@Compound(subtypeOf = Expr::class)
data class Ref(val id: Id) : Expr()

@Token
enum class BinaryOperator(private val str: String) {
    Plus("+"), Minus("-"), Times("*"), Div("/"), Mod("%"),
    And("&&"), Or("||"), Xor(""),
    EQ("=="), NEQ("!="),
    LE("<="), GE(">="), LO("<"), GR(">");

    override fun toString(): String = str

    companion object : TokenType<BinaryOperator> {
        private val operators = values().associateBy { it.str }

        override fun compile(token: String): CompileResult<BinaryOperator> =
            operators[token].okOrErr { "Invalid binary operator $token" }
    }
}

@Compound(subtypeOf = Expr::class)
data class BinaryExpression(val op: BinaryOperator, val left: Expr, val right: Expr) : Expr()

@Token
enum class UnaryOperator(private val str: String) {
    Not("!"), Minus("-");

    override fun toString(): String = str

    companion object : TokenType<UnaryOperator> {
        private val operators = values().associateBy { it.str }

        override fun compile(token: String): CompileResult<UnaryOperator> =
            operators[token].okOrErr { "Invalid binary operator $token" }
    }
}

@Compound(subtypeOf = Expr::class)
data class UnaryExpression(val op: UnaryOperator, val operand: Expr) : Expr()

@Alternative
@Expandable(StatementExpanderDelegator::class, subtypeOf = Statement::class)
@EditableList
sealed class Statement

@Compound(subtypeOf = Statement::class)
data class Assign(val name: Id, val value: Expr) : Statement()

@Compound(subtypeOf = Statement::class)
data class Swap(val left: Id, val right: Id) : Statement()

@UseEditor(NextExecutableEditor::class)
sealed class Executable

object End: Executable()

@Compound
data class Block(val statements: List<Statement>, val next: Executable) : Executable()

@Compound
data class Branch(val condition: Expr, val yes: Executable, val no: Executable)