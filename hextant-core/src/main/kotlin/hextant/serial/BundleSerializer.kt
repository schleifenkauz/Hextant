package hextant.serial

import bundles.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
internal object BundleSerializer : KSerializer<Bundle> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Bundle") {
        element<BundleEntry>("entries")
    }

    override fun serialize(encoder: Encoder, value: Bundle) {
        val entries = value.entries.map { (prop, value) -> BundleEntry(prop, value) }.toList()
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, serializer(), entries)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): Bundle = decoder.decodeStructure(descriptor) {
        check(decodeElementIndex(descriptor) == 0)
        val entries: List<BundleEntry> = decodeSerializableElement(descriptor, 0, serializer())
        return createBundle {
            for ((prop, value) in entries) {
                set(prop as Property<Any?, Any, Any>, value)
            }
        }
    }

}