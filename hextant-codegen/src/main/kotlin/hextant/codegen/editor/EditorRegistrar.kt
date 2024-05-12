package hextant.codegen.editor

import hextant.codegen.RegisterEditor
import javax.lang.model.element.TypeElement

internal object EditorRegistrar : EditorClassGen<RegisterEditor, TypeElement>() {
    override fun process(element: TypeElement, annotation: RegisterEditor) {
        EditorResolution.register(
            element, getTypeMirror(annotation::nodeType).toString()
        ) { hasEditorNullableResultType(element) }
    }
}