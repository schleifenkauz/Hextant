/**
 *@author Nikolaus Knop
 */

package hextant.codegen.editor

import hextant.codegen.*
import hextant.codegen.aspects.FeatureCollector
import krobot.api.*
import javax.lang.model.element.TypeElement

internal object ListEditorCodegen : EditorClassGen<ListEditor, TypeElement>() {
    override fun process(element: TypeElement, annotation: ListEditor) {
        val editorCls = getTypeMirror(annotation::editorCls).takeIf { it.toString() != None::class.qualifiedName }
        val editorClsName = editorCls?.toString() ?: getEditorClassName(element.asType())
        val nullable = isResultNullable(element.asType())
        val qn = extractQualifiedEditorClassName(annotation, element, classNameSuffix = "ListEditor")
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val resultType = type(element.simpleName.toString()).nullable(nullable)
        classModifiers(annotation.serializable, "$simpleName.Serializer::class").kotlinClass(simpleName)
            .primaryConstructor()
            .extends(type("ListEditor", resultType, type(editorClsName)))
            .body {
                +constructor("vararg editors" of editorClsName)
                    .delegate()
                    .body {
                        +"setInitialEditors(*editors)"
                    }
                +override.`fun`("createEditor") returns call(editorClsName)
                if (annotation.serializable) {
                    val editorsList = type("List", editorClsName)
                    addSerializerObject(simpleName) {
                        overrideDescriptor(simpleName) {
                            +call("element", typeArguments = listOf(editorsList), lit("editors"))
                        }
                        overrideSerialize(simpleName) {
                            +encodeElement(0, "value.editors.now".e)
                        }
                        overrideDeserialize {
                            +`val`("editors") of editorsList initializedWith decodeElement(0)
                            +call(simpleName, "*editors.toTypedArray()".e)
                        }
                    }
                }
            }
            .asFile {
                `package`(pkg)
                import(element.toString())
                import("hextant.core.editor.ListEditor")
                import(editorClsName)
                import("hextant.context.*")
                if (annotation.serializable) importSerializationPackages()
            }.saveToSourceRoot(generatedDir)
        FeatureCollector.generatedEditor("$pkg.$simpleName")
    }
}