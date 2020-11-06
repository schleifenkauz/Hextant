package hextant.serial

import bundles.Property
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlin.reflect.full.companionObjectInstance

@Suppress("EXPERIMENTAL_API_USAGE")
internal object BundleEntrySerializer : KSerializer<BundleEntry> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BundleEntry") {
        element<String>("propertyClass")
        element<String>("propertyName")
        element<String>("valueClass", isOptional = false)
        element("value", ContextualSerializer(Any::class).descriptor, isOptional = false)
    }

    override fun serialize(encoder: Encoder, value: BundleEntry) =
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.property.javaClass.name)
            encodeStringElement(descriptor, 1, value.property.name)
            if (value.value != null) {
                encodeStringElement(descriptor, 2, value.value.javaClass.name)
                val serializer = getSerializer(value.value::class)
                encodeNullableSerializableElement(descriptor, 3, serializer, value.value)
            }
        }

    override fun deserialize(decoder: Decoder): BundleEntry =
        decoder.decodeStructure(descriptor) {
            lateinit var propertyClass: String
            lateinit var propertyName: String
            lateinit var valueClass: String
            var value: Any? = null
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> propertyClass = decodeStringElement(descriptor, 0)
                    1 -> propertyName = decodeStringElement(descriptor, 1)
                    2 -> valueClass = decodeStringElement(descriptor, 2)
                    3 -> {
                        val serializer = getSerializer(valueClass.loadClass().kotlin)
                        value = decodeSerializableElement(descriptor, 3, serializer)
                    }
                    CompositeDecoder.DECODE_DONE -> break
                    else                         -> error("Unexpected index: $index")
                }
            }
            val prop = getPropertyInstance(propertyClass, propertyName) as Property<*, *, *>
            BundleEntry(prop, value)
        }

    private fun getPropertyInstance(propertyClass: String, name: String): Any? {
        val clz = propertyClass.loadClass()
        return clz.kotlin.objectInstance
            ?: clz.kotlin.companionObjectInstance
            ?: clz.getConstructor(String::class.java).newInstance(name)
    }
}