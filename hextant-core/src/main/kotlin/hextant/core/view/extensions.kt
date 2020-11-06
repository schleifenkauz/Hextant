/**
 * @author Nikolaus Knop
 */

package hextant.core.view

import hextant.core.EditorView
import hextant.serial.Snapshot

/**
 * Makes a snapshot of this [EditorView]
 */
@Suppress("UNCHECKED_CAST")
fun <V : EditorView> V.snapshot(recordClass: Boolean = false): Snapshot<V> {
    val snapshot = createSnapshot() as Snapshot<V>
    snapshot.record(this, recordClass)
    return snapshot
}
