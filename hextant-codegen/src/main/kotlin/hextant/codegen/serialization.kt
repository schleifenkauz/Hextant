package hextant.codegen

import krobot.api.*
import krobot.ast.Expr

internal inline fun ClassRobot.overrideDeserialize(body: BlockRobot.() -> Unit) {
    +override.`fun`(
        "deserialize",
        "decoder" of "Decoder",
    ) returns call("decoder.decodeStructure", "descriptor".e, closure {
        body()
    })
}

internal inline fun ClassRobot.overrideSerialize(
    simpleName: String,
    body: BlockRobot.() -> Unit
) {
    +override.`fun`(
        "serialize",
        "encoder" of "Encoder",
        "value" of simpleName
    ) returns call("encoder.encodeStructure", "descriptor".e, closure {
        body()
    })
}

internal inline fun ClassRobot.overrideDescriptor(simpleName: String, build: BlockRobot.() -> Unit) {
    override.`val`("descriptor").of(type("SerialDescriptor"))
        .accessors {
            get = call(
                "buildClassSerialDescriptor",
                lit(simpleName),
                closure {
                    build()
                })
        }
}

internal inline fun ClassRobot.addSerializerObject(simpleName: String, build: ClassRobot.() -> Unit) {
    +`object`("Serializer").implements(type("KSerializer", simpleName)).body(build)
}

internal fun decodeElement(index: Int) = call(
    "decodeSerializableElement",
    "descriptor".e,
    lit(index),
    call("kotlinx.serialization.serializer")
)

internal fun encodeElement(index: Int, value: Expr): Expr = call(
    "encodeSerializableElement",
    "descriptor".e,
    lit(index),
    call("kotlinx.serialization.serializer"),
    value
)