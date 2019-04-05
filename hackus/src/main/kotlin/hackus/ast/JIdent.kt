/**
 *@author Nikolaus Knop
 */

package hackus.ast

import hextant.*
import hextant.core.editable.TokenType

class JIdent private constructor(val name: String) {
    companion object : TokenType<JIdent> {
        private val regex = Regex("[_a-zA-Z][_\$a-zA-Z0-9]*")

        override fun compile(tok: String): CompileResult<JIdent> =
            tok.takeIf { it.matches(regex) }
                .okOrErr { "Invalid identifier $tok" }
                .map { JIdent(it) }
    }

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JIdent

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}