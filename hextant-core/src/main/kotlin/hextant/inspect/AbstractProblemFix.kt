/**
 *@author Nikolaus Knop
 */

package hextant.inspect

/**
 * Skeletal implementation for [ProblemFix]
 * * Implements [toString]
 */
abstract class AbstractProblemFix : ProblemFix {
    final override fun toString(): String = "Problem Fix: $description"
}