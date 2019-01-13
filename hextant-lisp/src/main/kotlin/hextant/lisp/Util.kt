package hextant.lisp

object Util {
    fun lambdaToString(parameters: List<String>, body: SExpr) = buildString {
        append("lambda")
        for (p in parameters) {
            append(p)
            append(" -> ")
        }
        append(body)
    }

    fun isValidIdentifier(str: String): Boolean = str.isNotEmpty() &&
            str.first().isValidIdentifierStart() && str.drop(1).all { c -> c.isValidIdentifierPart() }

    private fun Char.isValidIdentifierStart() = this != '#' && this != ',' && !isDigit()

    private fun Char.isValidIdentifierPart(): Boolean = !isWhitespace()
}