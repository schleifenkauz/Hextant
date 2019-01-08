package hextant.lisp

class FileScope(private val imports: List<Program>, private val program: Program) {
    fun lookup(identifier: Identifier): SExpr {
        val defined = program[identifier]
        if (defined != null) return defined
        for (imported in imports) {
            val import = imported[identifier]
            if (import != null) return import
        }
        return BuiltIns.get(identifier) ?: throw LispRuntimeError("Cannot find $identifier")
    }
}