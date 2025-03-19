package hextant.codegen.editor

import hextant.codegen.*
import krobot.api.*
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

internal object ChoiceEditorCodegen : EditorClassGen<Choice, Element>() {
    override fun preprocess(element: Element, annotation: Choice) {
        EditorResolution.register(nodeType(element), extractQualifiedEditorClassName(annotation, element)) { false }
    }

    override fun process(element: Element, annotation: Choice) {
        val nodeType = nodeType(element)
        val resultType = nodeType.simpleName.t
        val choicesFunc = choicesFunc(element)
        val qn = extractQualifiedEditorClassName(annotation, element)
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        classModifiers(annotation.serializable, "$simpleName.Serializer::class").kotlinClass(simpleName)
            .primaryConstructor()
            .extends(type("SimpleChoiceEditor", resultType))
            .implementEditorOfSuperType(annotation, resultType)
            .implements(
                "JsonSerializer<${nodeType.simpleName}>",
                by = "KJsonSerializer(kotlinx.serialization.serializer<${nodeType.simpleName}>())".e
            )
            .body {
                +constructor(
                    "initialValue" of resultType
                ).delegate().body {
                    +"selectInitial(initialValue)"
                }
                +override.`fun`("setupDefaultState").body {
                    val default = annotation.initialValue.takeIf { it != DEFAULT } ?: "$choicesFunc[0]"
                    +"selectInitial"(default.e)
                }
                +override.`fun`("choices") returns choicesFunc.e
                if (annotation.serializable) {
                    importSerializationPackages()
                    addSerializerObject(simpleName) {
                        overrideDescriptor(simpleName) {
                            +call("element", typeArguments = listOf(resultType), lit("selected"))
                        }
                        overrideDeserialize {
                            +`val`("selected") of resultType initializedWith decodeElement(0)
                            +call(simpleName, "selected".e)
                        }
                        overrideSerialize(simpleName) {
                            +encodeElement(0, "value.result.get()".e)
                        }
                    }
                }
            }
            .asFile {
                `package`(pkg)
                import("hextant.core.editor.*")
                import("hextant.context.*")
                import("hextant.serial.*")
                import(nodeType)
                import(processingEnv.fqName(element))
            }
            .saveToSourceRoot(generatedDir)
        generatedEditor(nodeType, qn)
    }

    private fun nodeType(element: Element) = when (element) {
        is TypeElement -> element
        is ExecutableElement -> element.returnType.asTypeElement()
        else -> throw ProcessingException("annotation @Choice applied to invalid element $element")
    }

    private fun choicesFunc(element: Element) = when (element) {
        is TypeElement -> "${element.simpleName}.entries"
        is ExecutableElement -> "${element.simpleName}()"
        else -> throw ProcessingException("annotation @Choice applied to invalid element $element")
    }
}