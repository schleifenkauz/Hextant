/**
 *@author Nikolaus Knop
 */

package hextant.codegen.editor

import hextant.codegen.ListEditor
import hextant.codegen.None
import hextant.codegen.aspects.FeatureCollector
import hextant.codegen.splitPackageAndSimpleName
import krobot.api.*
import javax.lang.model.element.TypeElement

internal object ListEditorCodegen : EditorClassGen<ListEditor, TypeElement>() {
    override fun process(element: TypeElement, annotation: ListEditor) {
        val editorCls = getTypeMirror(annotation::editorCls).takeIf { it.toString() != None::class.qualifiedName }
        val editorClsName = editorCls?.toString() ?: getEditorClassName(element.asType())
        val simpleName = element.simpleName.toString()
        val nullable = isResultNullable(element.asType())
        val qn = extractQualifiedEditorClassName(annotation, element, classNameSuffix = "ListEditor")
        val (pkg, name) = splitPackageAndSimpleName(qn)
        classModifiers(annotation.serializable).kotlinClass(name)
            .primaryConstructor()
            .extends(type("ListEditor", type(simpleName).nullable(nullable), type(editorClsName)))
            .body {
                +constructor("vararg editors" of editorClsName)
                    .delegate()
                    .body {
                        +"setInitialEditors(*editors)"
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