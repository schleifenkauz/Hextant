/**
 *@author Nikolaus Knop
 */

package hextant.codegen.editor

import hextant.codegen.EditableList
import hextant.codegen.None
import hextant.codegen.aspects.FeatureCollector
import hextant.codegen.splitPackageAndSimpleName
import krobot.api.*
import javax.lang.model.element.TypeElement

internal object ListEditorCodegen : EditorClassGen<EditableList, TypeElement>() {
    override fun process(element: TypeElement, annotation: EditableList) {
        val editorCls = getTypeMirror(annotation::editorCls).takeIf { it.toString() != None::class.qualifiedName }
        val editorClsName = editorCls?.toString() ?: getEditorClassName(element.asType())
        val simpleName = element.simpleName.toString()
        val nullable = isResultNullable(element.asType())
        val qn = extractQualifiedEditorClassName(annotation, element, classNameSuffix = "ListEditor")
        val (pkg, name) = splitPackageAndSimpleName(qn)
        kotlinClass(name)
            .primaryConstructor("context" of "Context")
            .extends(type("ListEditor", type(simpleName).nullable(nullable), type(editorClsName)), "context".e)
            .body {
                +constructor("context" of "Context", "vararg editors" of editorClsName)
                    .delegate("context".e)
                    .body {
                        +`for`("i", `in` = "editors".e select "indices") {
                            +`val`("e") initializedWith "editors".e["i".e]
                            +call("addAt", "i".e, "e".e)
                        }
                    }
                +override.`fun`("createEditor") returns call(editorClsName, get(annotation.childContext))
            }
            .asFile {
                `package`(pkg)
                import(element.toString())
                import("hextant.core.editor.ListEditor")
                import(editorClsName)
                import("hextant.context.*")
            }.saveToSourceRoot(generatedDir)
        FeatureCollector.generatedEditor("$pkg.$name")
    }
}