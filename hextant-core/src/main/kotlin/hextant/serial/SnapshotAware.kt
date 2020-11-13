/**
 * @author Nikolaus Knop
 */

package hextant.serial

/**
 * Represents objects that are able to make 'snapshot' of themselves that allows reconstructing them later.
 */
interface SnapshotAware {
    /**
     * Create a [Snapshot] that is able to record the state of this object.
     *
     * Most likely you don't want to use this method but instead [hextant.serial.snapshot].
     * This method only creates a snapshot object.
     * The latter uses this method to create a snapshot and then saves the state of the object
     * to the created snapshot.
     */
    fun createSnapshot(): Snapshot<*> =
        throw UnsupportedOperationException("creating a snapshot is not supported for $this")
}