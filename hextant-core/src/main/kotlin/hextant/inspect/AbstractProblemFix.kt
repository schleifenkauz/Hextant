/**
 *@author Nikolaus Knop
 */

package hextant.inspect

/**
 * Skeletal implementation for [ProblemFix]
 * * Implements [toString]
 */
abstract class AbstractProblemFix<in T : Any> : ProblemFix<T> {
    final override fun toString(): String = "Problem Fix: $description"
}