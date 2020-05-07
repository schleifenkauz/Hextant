/**
 * @author Nikolaus Knop
 */

package hextant

/**
 * Used to save the state of an object at given point of time.
 * Snapshots can be used to reduce the memory impact of saving an object for later.
 * @param Original the type of the original object.
 * @param Info the external information needed to reconstruct the original object.
 */
interface Snapshot<out Original, in Info> {
    /**
     * Reconstruct the original object from this snapshot.
     */
    fun reconstruct(info: Info): Original
}