package hextant.lisp

object Util {
    fun lambdaToString(parameters: List<String>, body: SExpr) = buildString {
        for (p in parameters) {
            append(p)
            append(" -> ")
        }
        append(body)
    }
}