/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.inspect

internal class ProblemFixImpl(
    override val description: String,
    private val doFix: () -> Unit,
    private val applicable: () -> Boolean
) : AbstractProblemFix() {
    override fun fix() {
        doFix()
    }

    override fun isApplicable() = applicable()
}