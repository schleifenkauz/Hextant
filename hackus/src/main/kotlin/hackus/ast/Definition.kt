/**
 *@author Nikolaus Knop
 */

package hackus.ast

sealed class Definition {
    data class Terminal(val name: FQName) : Definition()

    data class Alternative(val name: FQName, val alternatives: List<FQName>) : Definition()
}