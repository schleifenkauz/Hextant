package hextant.lisp

class FileScope(private val imports: Map<Identifier, Value>, private val program: Program) {
    fun lookup(identifier: Identifier): Value {
        val defined = program[identifier]
        if (defined != null) return defined
        val imported = imports[identifier]
        if (imported != null) return imported
        return BuiltIns.get(identifier) ?: throw LispRuntimeError("Cannot find $identifier")
    }

    companion object {
        val empty = FileScope(emptyMap(), emptyMap())
    }
}