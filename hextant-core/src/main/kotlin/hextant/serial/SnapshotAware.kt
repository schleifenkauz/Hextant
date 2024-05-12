/**
 * @author Nikolaus Knop
 */

package hextant.serial

import hextant.context.Context
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Represents objects that are able to make 'snapshot' of themselves that allows reconstructing them later.
 */
@Serializable(with = SnapshotAware.Serializer::class)
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

    object Serializer : KSerializer<SnapshotAware> {
        lateinit var reconstructionContext: Context

        override val descriptor: SerialDescriptor
            get() = Snapshot.Serializer.descriptor

        override fun deserialize(decoder: Decoder): SnapshotAware {
            val snapshot = decoder.decodeSerializableValue(Snapshot.Serializer)
            val original = snapshot.reconstruct(reconstructionContext)
            return original as SnapshotAware
        }

        override fun serialize(encoder: Encoder, value: SnapshotAware) {
            val snapshot = value.snapshot(recordClass = true)
            encoder.encodeSerializableValue(Snapshot.Serializer, snapshot)
        }
    }
}