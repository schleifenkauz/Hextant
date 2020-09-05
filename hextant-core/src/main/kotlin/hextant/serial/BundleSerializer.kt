package hextant.serial

import bundles.Bundle
import bundles.Property
import kserial.*

internal object BundleSerializer : InplaceSerializer<Bundle> {
    override fun serialize(obj: Bundle, output: Output) {
        output.writeInt(obj.entries.count())
        for ((prop, value) in obj.entries) {
            output.writeObject(prop)
            output.writeObject(value)
        }
    }

    override fun deserialize(obj: Bundle, input: Input) {
        val size = input.readInt()
        repeat(size) {
            val prop = input.readTyped<Property<Any?, Any, Any>>()
            val value = input.readObject()
            obj[prop] = value
        }
    }
}