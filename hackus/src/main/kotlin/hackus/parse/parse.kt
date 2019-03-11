/**
 * @author Nikolaus Knop
 */

package hackus.parse

import hackus.ast.Definition
import java.io.BufferedReader

fun parseDefinitions(input: BufferedReader): List<Definition> = input.use {
    it.lineSequence().mapNotNull { l ->
        when {
            l.startsWith('#') -> null
            l.isEmpty()       -> null
            else              -> parseDefinition(l)
        }
    }.toList()
}

fun parseDefinition(l: String): Definition {
    TODO("not implemented")
}

fun parseFQName(str: String) {

}