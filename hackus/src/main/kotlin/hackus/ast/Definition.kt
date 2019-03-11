/**
 *@author Nikolaus Knop
 */

package hackus.ast

sealed class Definition {
    data class Terminal(val tokenName: FQName, val editableName: FQName) : Definition()

    data class Alternative(val name: FQName, val alternatives: List<FQName>) : Definition()
}