package hextant.core.editor

object NullTokenType : TokenType<Nothing?> {
    override fun compile(token: String): Nothing? = null
}