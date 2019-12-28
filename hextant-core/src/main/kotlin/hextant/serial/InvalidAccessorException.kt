/**
 *@author Nikolaus Knop
 */

package hextant.serial

/**
 * Thrown when an [EditorAccessor] cannot be resolved from a specific parent
 */
class InvalidAccessorException(accessor: EditorAccessor) : RuntimeException("No sub editor with accessor $accessor")