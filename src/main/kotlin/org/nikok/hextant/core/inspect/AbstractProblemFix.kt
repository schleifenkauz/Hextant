/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.inspect

/**
 * Skeletal implementation for [ProblemFix]
 * * Implements [toString]
*/
abstract class AbstractProblemFix: ProblemFix {
    final override fun toString(): String = "Problem Fix: $description"
}