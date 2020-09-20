package hextant.codegen.editor

import hextant.codegen.*
import krobot.api.*
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

internal object CompoundEditorCodegen : EditorClassGen<Compound>() {
    override fun process(element: TypeElement, annotation: Compound) {
        val name = element.simpleName.toString()
        val qn = extractQualifiedEditorClassName(annotation, element)
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val members = processingEnv.elementUtils.getAllMembers(element)
        val constructors = members.filter {
            it.simpleName.toString() == "<init>"
        }
        ensure(constructors.size == 1) { "Class $element with annotation @Compound has ${constructors.size} constructors" }
        val primary = constructors[0] as ExecutableElement
        val file = kotlinClass(
            pkg,
            imports = {
                import("hextant.core.editor.*")
                import(element.toString())
                import("hextant.context.*")
                import("validated.reaktive.*")
            },
            name = simpleName,
            primaryConstructor = {
                "context" of "Context"
            },
            inheritance = {
                extend(
                    "CompoundEditor".t.parameterizedBy { invariant(name) },
                    "context".e
                )
                implementEditorOfSuperType(annotation, name)
            }
        ) {
            val names = primary.parameters.map { it.toString() }
            for (p in primary.parameters) {
                val comp = p?.getAnnotation(Component::class.java)
                val custom = comp?.let { getTypeMirror { it.editor } }?.toString()
                    .takeIf { it != "hextant.codegen.None" }
                val editorCls = custom ?: getEditorClassName(p.asType())
                addVal(p.simpleName.toString()) {
                    by(call("child", call(editorCls, comp?.childContext?.e ?: "context".e)))
                }
            }
            addVal(
                "result",
                "ReactiveValidated".t.parameterizedBy { invariant(name) }, { override() }) {
                initializeWith(
                    call("composeReactive", *names.mapToArray { n -> n.e select "result" }, "::$name".e)
                )
            }
        }
        writeKotlinFile(file)
        generatedEditor(element, "$pkg.$simpleName")
    }
}