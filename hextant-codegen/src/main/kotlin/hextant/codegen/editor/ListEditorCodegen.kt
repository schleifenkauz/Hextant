/**
 *@author Nikolaus Knop
 */

package hextant.codegen.editor

import hextant.codegen.*
import hextant.codegen.aspects.FeatureCollector
import krobot.api.*
import javax.lang.model.element.TypeElement

internal object ListEditorCodegen : EditorClassGen<EditableList>() {
    override fun process(element: TypeElement, annotation: EditableList) {
        val editorCls = getTypeMirror(annotation::editorCls).takeIf { it.toString() != None::class.qualifiedName }
        val editorClsName = editorCls?.toString() ?: getEditorClassName(element.asType())
        val simpleName = element.simpleName.toString()
        val qn = extractQualifiedEditorClassName(annotation, element, classNameSuffix = "ListEditor")
        val (pkg, name) = splitPackageAndSimpleName(qn)
        val file = kotlinClass(
            pkg, {
                import(element.toString())
                import("hextant.core.editor.ListEditor")
                import(editorClsName)
                import("hextant.context.*")
            },
            name,
            primaryConstructor = { "context" of "Context" },
            inheritance = {
                extend(type("ListEditor").parameterizedBy {
                    invariant(simpleName)
                    invariant(editorClsName)
                }, "context".e)
            }
        ) {
            addConstructor(
                {
                    "context" of "Context"
                    "vararg editors" of editorClsName
                },
                "context".e
            ) {
                addFor("i", "editors".e select "indices") {
                    addVal("e") initializedWith "editors".e["i".e]
                    callFunction("addAt", {}, "i".e, "e".e)
                }
            }
            addConstructor(
                {
                    "context" of "Context"
                    "elements" of type("List").parameterizedBy { invariant(simpleName) }
                },
                "context".e
            ) {
                addFor("i", "elements".e select "indices") {
                    addVal("e") initializedWith ("context".e
                        .call("createEditor", "elements".e["i".e])
                        .cast(type(editorClsName)))
                    callFunction("addAt", {}, "i".e, "e".e)
                }
            }
            addSingleExprFunction("createEditor", { override() }) { call(editorClsName, "context".e) }
        }
        writeKotlinFile(file)
        FeatureCollector.generatedEditor("$pkg.$name")
    }
}