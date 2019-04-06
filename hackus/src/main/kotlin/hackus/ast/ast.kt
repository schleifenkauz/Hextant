/**
 *@author Nikolaus Knop
 */

package hackus.ast

data class SubNode(val name: JIdent, val type: FQName)

sealed class RightSide {
    data class Terminal(val tokenTypeCls: FQName) : RightSide()

    data class Alternative(val alternatives: List<FQName>) : RightSide()

    data class Compound(val subNodes: List<SubNode>): RightSide()
}

data class Definition(val name: FQName, val rightSide: RightSide)