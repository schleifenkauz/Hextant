/**
 *@author Nikolaus Knop
 */

package hextant.bundle

/**
 * @thrown if a [Property] is queried on a [Bundle] that doesn't have this property
 */
class NoSuchPropertyException(msg: String) : NoSuchElementException(msg)