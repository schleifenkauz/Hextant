/**
 * @author Nikolaus Knop
 */

package hextant.serial

/**
 * Represents objects that are able to make 'snapshot' of themselves that allows reconstructing them later.
 */
interface SnapshotAware {
    /**
     * Create a [Snapshot] of the current state of this object.
     */
    fun createSnapshot(): Snapshot<*> =
        throw UnsupportedOperationException("creating a snapshot is not supported for $this")
}