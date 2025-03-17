package hextant.codegen.editor

import hextant.codegen.*
import krobot.api.*
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

internal object CompoundEditorCodegen : EditorClassGen<Compound, Element>() {
    override fun preprocess(element: Element, annotation: Compound) {
        val qn = extractQualifiedEditorClassName(annotation, element)
        val function = extractFunction(element)
        val result = function.returnType().asTypeElement()
        if (annotation.register) {
            EditorResolution.register(result, qn) { isNodeKindNullable(annotation) || isResultNullable(function) }
        }
    }

    private fun isResultNullable(function: ExecutableElement) =
        function.parameters.any { p -> isResultNullable(p.asType(), p.getAnnotation<Component>()) }

    override fun process(element: Element, annotation: Compound) {
        val qn = extractQualifiedEditorClassName(annotation, element)
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val function = extractFunction(element)
        val resultClass = function.returnType().asTypeElement()
        val result = resultClass.simpleName.toString()
        val resultType = type(result).nullable(isNodeKindNullable(annotation) || isResultNullable(function))
        val functionName = getFunctionName(function)
        val parameters = function.parameters.map { p ->
            val ann = p.getAnnotation<Component>()
            val editorCls = getEditorClassName(p.asType(), ann)
            `val` parameter ("${p.simpleName}") of type(editorCls) default call(editorCls)
        }
        val componentNames = function.parameters.map { p -> p.simpleName.toString() }
        classModifiers(annotation.serializable, "$simpleName.Serializer::class").kotlinClass(simpleName)
            .primaryConstructor(parameters)
            .extends(type("CompoundEditor", resultType.contextualSerialization(annotation.serializable)))
            .implementEditorOfSuperType(annotation, resultType.contextualSerialization(annotation.serializable))
            .body {
                final.override.lateinit.`var`("result").of(type("ReactiveValue", resultType)).accessors {
                    private.set
                }
                +override.`fun`("doInitialize").body {
                    "result" assign call("composeResult", closure {
                        +call(functionName, componentNames.map { component -> get(component) select "now" })
                    })
                }
                +override.`fun`(
                    "locate",
                    "parent" of "hextant.core.Editor<*>?",
                    "accessor" of "hextant.serial.EditorAccessor",
                    "expander" of "hextant.core.editor.Expander<*, *>?"
                ).body {
                    +"super.locate(parent, accessor, expander)"
                    for (component in componentNames) {
                        +"${component}.locate(parent = this, hextant.serial.PropertyAccessor(\"${component}\"))"
                    }
                }
                if (annotation.serializable) {
                    importSerializationPackages()
                    addSerializerObject(simpleName) {
                        overrideDescriptor(simpleName) {
                            for (param in parameters) {
                                +call("element", typeArguments = listOf(param.type!!), lit(param.name))
                            }
                        }
                        overrideSerialize(simpleName) {
                            for ((idx, param) in parameters.withIndex()) {
                                +encodeElement(idx, "value.${param.name}".e)
                            }
                        }
                        overrideDeserialize {
                            for (param in parameters) {
                                +lateinit.`var`(param.name).of(param.type!!)
                            }
                            +`while`("true".e) {
                                +`val`("index") initializedWith call("decodeElementIndex", "descriptor".e)
                                +`when`("index".e) {
                                    for ((idx, param) in parameters.withIndex()) {
                                        equalTo(lit(idx)) then {
                                            param.name assign decodeElement(idx)
                                        }
                                    }
                                }
                            }
                            +call(simpleName, parameters.map { p -> p.name.e })
                        }
                    }
                }
            }
            .asFile {
                `package`(pkg)
                import("hextant.core.editor.*")
                import(resultClass.toString())
                import("hextant.context.*")
                import("reaktive.value.ReactiveValue")
                import(processingEnv.fqName(element))
            }.saveToSourceRoot(generatedDir)
        if (element is TypeElement) generatedEditor(element, qn)
    }

    private fun extractFunction(element: Element) = when (element) {
        is TypeElement -> processingEnv.elementUtils.getAllMembers(element)
            .firstOrNull { it.simpleName.toString() == "<init>" } as ExecutableElement?
            ?: fail("Class $element has no constructor")

        is ExecutableElement -> element
        else -> fail("Illegal annotation target for @Compound: $element")
    }
}

