/**
 * @author Nikolaus Knop
 */

package hextant.codegen

import com.google.auto.service.AutoService
import hextant.*
import hextant.base.AbstractEditor
import hextant.core.TokenType
import hextant.core.editor.*
import hextant.core.view.TokenEditorView
import krobot.api.*
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.*
import javax.tools.Diagnostic.Kind.ERROR
import kotlin.reflect.KClass

@Suppress("unused")
@AutoService(Processor::class)
class AnnotationProcessor : AbstractProcessor() {
    private class ProcessingException(msg: String) : Exception(msg)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(Token::class.qualifiedName!!)

    private fun getOneAnnotation(
        element: Element,
        annotationClasses: Set<KClass<out Annotation>>
    ): Annotation {
        val annotations = annotationClasses.mapNotNull { element.getAnnotation(it.java) }
        if (annotations.isEmpty()) {
            error("$element is not annotated with any editor codegen annotation")
        } else if (annotations.size > 1) {
            error("$element is annotated with more than one editor codegen annotation")
        }
        return annotations.first()
    }

    /**
     * Looks up the annotation annotating the given [element] and delegates to [extractQualifiedEditorClassName]
     */
    private fun lookupQualifiedEditorClassName(element: Element): String {
        element.getAnnotation(UseEditor::class.java)?.let { ann ->
            val tm = getTypeMirror(ann::cls)
            return tm.toString()
        }
        val ann = getOneAnnotation(element, setOf(Token::class, Compound::class, Expandable::class))
        val suffix = if (ann is Expandable) "Expander" else "Editor"
        return extractQualifiedEditorClassName(ann, element, classNameSuffix = suffix)
    }

    private fun extractQualifiedEditorClassName(
        ann: Annotation,
        element: Element,
        packageSuffix: String = "editor",
        classNameSuffix: String = "Editor"
    ): String {
        val configured = ann.qualifiedEditorClassName
        if (configured != null) return configured
        val pkg = processingEnv.elementUtils.getPackageOf(element)
        return "$pkg.$packageSuffix.${element.simpleName}$classNameSuffix"
    }

    private inline fun <reified A : Annotation> RoundEnvironment.processAnnotations(
        process: (element: TypeElement, annotation: A) -> Unit
    ) {
        getElementsAnnotatedWith(A::class.java).forEach { element ->
            val annotation = element.getAnnotation(A::class.java)
            process(element as TypeElement, annotation)
        }
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            with(roundEnv) {
                processAnnotations(::genTokenEditorClass)
                processAnnotations(::genCompoundEditorClass)
                processAnnotations(::genAlternativeInterface)
                processAnnotations(::genExpanderClass)
                processAnnotations(::genListEditorClass)
            }
        } catch (e: ProcessingException) {
            processingEnv.messager.printMessage(ERROR, e.message)
        } catch (e: Throwable) {
            processingEnv.messager.printMessage(ERROR, "Unexpected error ${e.message}")
            val w = StringWriter()
            val p = PrintWriter(w)
            e.printStackTrace(p)
            processingEnv.messager.printMessage(ERROR, w.toString())
        }
        return true
    }

    private fun genListEditorClass(annotated: Element, annotation: EditableList) {
        val editorCls = getTypeMirror(annotation::editorCls).takeIf { it.toString() != None::class.qualifiedName }
        val editorClsName = editorCls?.toString() ?: getEditorClassName(annotated.asType(), null)
        val simpleName = annotated.simpleName.toString()
        val qn = extractQualifiedEditorClassName(annotation, annotated, classNameSuffix = "ListEditor")
        val (pkg, name) = splitPackageAndSimpleName(qn)
        val file = kotlinClass(
            pkg, {
                import(annotated.toString())
                import<ListEditor<*, *>>()
                import(editorClsName)
                import("hextant.*")
            },
            name,
            primaryConstructor = { "context" of "Context" },
            inheritance = {
                extend(type("ListEditor").parameterizedBy {
                    covariant(simpleName)
                    covariant(editorClsName)
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
                    "elements" of type("List").parameterizedBy { covariant(simpleName) }
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
        writeToFile(pkg, name, file)
    }

    private fun splitPackageAndSimpleName(qualifiedName: String): Pair<String?, String> {
        val idx = qualifiedName.lastIndexOf('.')
        if (idx == -1) return null to qualifiedName //No package part
        val pkg = qualifiedName.take(idx)
        val simpleName = qualifiedName.drop(idx + 1)
        return pkg to simpleName
    }

    private fun genTokenEditorClass(annotated: Element, annotation: Token) {
        val name = annotated.simpleName.toString()
        val qn = extractQualifiedEditorClassName(annotation, annotated)
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val file = kotlinClass(
            pkg,
            {
                import<TokenEditor<*, *>>()
                import(annotated.toString())
                import<Context>()
                import<TokenType<*>>()
                import<TokenEditorView>()
            },
            simpleName,
            primaryConstructor = { "context" of "Context"; "text" of type("String") },
            inheritance = {
                extend(
                    "TokenEditor".t.parameterizedBy {
                        covariant(name)
                        covariant("TokenEditorView")
                    },
                    "context".e,
                    "text".e
                )
                implementEditorOfSuperType(annotation, name)
            }
        ) {
            addConstructor({ "context" of "Context" }, "context".e, stringLiteral(""))
            addConstructor({
                "context" of "Context"
                "value" of name
            }, "context".e, "value".e call "toString")
            addSingleExprFunction(
                "compile",
                { override() },
                parameters = { "token" of "String" }) { name.e.call("compile", "token".e) }
        }
        writeToFile(pkg, simpleName, file)
    }

    private fun KInheritanceRobot.implementEditorOfSuperType(
        annotation: Annotation,
        simpleName: String
    ) {
        val supertype = getTypeMirror(annotation::subtypeOf).toString()
        if (supertype != None::class.qualifiedName) {
            val el = processingEnv.elementUtils.getTypeElement(supertype)
            val ann = el.getAnnotation(Alternative::class.java)
            if (ann == null) error("No annotation of type Alternative on $supertype")
            else {
                val editorQN = extractQualifiedEditorClassName(ann, el)
                implement(editorQN.t.parameterizedBy { covariant(simpleName) })
            }
        }
    }

    private fun error(msg: String): Nothing {
        throw ProcessingException(msg)
    }

    private inline fun getTypeMirror(classAccessor: () -> KClass<*>): TypeMirror {
        return try {
            val cls = classAccessor()
            val name = cls.qualifiedName
            val element = processingEnv.elementUtils.getTypeElement(name)
            processingEnv.typeUtils.getDeclaredType(element)
        } catch (ex: MirroredTypeException) {
            ex.typeMirror
        }
    }

    private fun writeToFile(pkg: String?, simpleName: String, file: KotlinFile) {
        val generatedDir = processingEnv.options["kapt.kotlin.generated"]!!
        val packages = pkg?.split('.')?.toTypedArray() ?: emptyArray()
        val path = Paths.get(generatedDir, *packages, "$simpleName.kt")
        Files.createDirectories(path.parent)
        file.writeTo(path)
    }

    private fun genCompoundEditorClass(annotated: TypeElement, annotation: Compound) {
        val name = annotated.simpleName.toString()
        val qn = extractQualifiedEditorClassName(annotation, annotated)
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val members = processingEnv.elementUtils.getAllMembers(annotated)
        val constructors = members.filter {
            it.simpleName.toString() == "<init>"
        }
        if (constructors.size != 1) {
            error("Class $annotated with annotation @Compound has ${constructors.size} constructors")
        }
        val primary = constructors[0] as ExecutableElement
        val file = kotlinClass(
            pkg,
            imports = {
                import<AbstractEditor<*, *>>()
                import(annotated.toString())
                import("hextant.*")
                import<EditorView>()
                import("reaktive.value.now")
            },
            name = simpleName,
            primaryConstructor = {
                "context" of "Context"
                for (p in primary.parameters) {
                    val editorCls = getEditorClassName(p.asType(), p)
                    val n = p.simpleName.toString()
                    n of editorCls
                }
            },
            inheritance = {
                extend(
                    "AbstractEditor".t.parameterizedBy { covariant(name); covariant("EditorView") },
                    "context".e
                )
                implementEditorOfSuperType(annotation, name)
            }
        ) {
            val names = primary.parameters.map { it.toString() }
            val componentClasses = primary.parameters.map { getEditorClassName(it.asType(), it) }
            addConstructor(
                { "context" of "Context" },
                *componentClasses.mapToArray { call(it, "context".e) },
                "context".e
            )
            addConstructor(
                { "context" of "Context"; "edited" of name },
                *primary.parameters.mapToArray { p ->
                    val editorCls = getEditorClassName(p.asType(), p)
                    val n = p.simpleName.toString()
                    call(editorCls, "context".e, "edited".e select n)
                },
                "context".e
            )
            for (n in names) {
                addVal(n) { initializeWith(n.e.call("moveTo", "context".e)) }
            }
            init {
                for (n in names) {
                    callFunction("child", {}, "this".e select n)
                }
            }
            val components = names.map { it.e }
            addVal(
                "result",
                "EditorResult".t.parameterizedBy { covariant(name) }, { override() }) {
                initializeWith(
                    call(
                        "result",
                        *components.toTypedArray(),
                        lambda(body = call("compile", lambda {
                            for (n in names) {
                                addVal("${n}Res") initializedWith (n.e select "result" select "now" call "orTerminate")
                            }
                            callFunction("ok", {}, call(name, {}, *names.mapToArray { n -> "${n}Res".e }))
                        }))
                    )
                )
            }

        }
        writeToFile(pkg, simpleName, file)
    }

    private fun getEditorClassName(tm: TypeMirror, p: VariableElement?): String {
        val useEditor = p?.getAnnotation(UseEditor::class.java)
        if (useEditor != null) {
            val cls = getTypeMirror(useEditor::cls)
            return cls.toString()
        }
        val t = checkNonPrimitive(tm)
        val e = processingEnv.typeUtils.asElement(t)
        if (e.toString() == "java.util.List") {
            val elementType = checkNonPrimitive(t.typeArguments[0]).asElement()
            val ann = elementType.getAnnotation(EditableList::class.java)
            return extractQualifiedEditorClassName(ann, elementType, classNameSuffix = "ListEditor")

        }
        return lookupQualifiedEditorClassName(e)
    }

    private fun checkNonPrimitive(t: TypeMirror): DeclaredType {
        if (t is DeclaredType) return t
        if (t is WildcardType) return checkNonPrimitive(t.extendsBound)
        error("Invalid component type $t(${t::class})")
    }

    private fun genAlternativeInterface(annotated: Element, annotation: Alternative) {
        val name = annotated.simpleName.toString()
        val qn = extractQualifiedEditorClassName(annotation, annotated)
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val typeParam = name.take(1)
        val file = kotlinInterface(
            pkg, {
                import<Editor<*>>()
                import(annotated.toString())
            },
            simpleName,
            typeParameters = { outvariant(typeParam, upperBound = name.t) },
            inheritance = { implement("Editor".t.parameterizedBy { covariant(typeParam) }) }
        )
        writeToFile(pkg, simpleName, file)
    }

    private fun genExpanderClass(annotated: Element, annotation: Expandable) {
        val name = annotated.simpleName.toString()
        val qn = extractQualifiedEditorClassName(annotation, annotated, classNameSuffix = "Expander")
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val ann = annotated.getAnnotation(Alternative::class.java)
        val commonInterface = extractQualifiedEditorClassName(ann, annotated)
        val editorType = commonInterface.t.parameterizedBy { covariant(name) }
        val delegator = getTypeMirror(annotation::delegator)
        val file = kotlinClass(
            pkg, {
                import("hextant.*")
                import<Expander<*, *>>()
                import(annotated.toString())
                import(delegator.toString())
                import<EditorFactory>()
            },
            simpleName,
            primaryConstructor = {
                "context" of "Context"
                "editor" of editorType.nullable()
            },
            inheritance = {
                extend(
                    "Expander".t.parameterizedBy {
                        covariant(name)
                        covariant(editorType)
                    },
                    "context".e,
                    "editor".e
                )
                implementEditorOfSuperType(annotation, name)
            }
        ) {
            val delegate = delegator.toString().e call "getDelegate"
            addConstructor({ "context" of "Context" }, "context".e, "null".e)
            addConstructor(
                { "context" of "Context"; "edited" of name }, "context".e,
                "context".e.call("createEditor", "edited".e) cast editorType
            )
            addVal("config", modifiers = { private() }) { initializeWith(delegate) }
            addSingleExprFunction(
                "expand",
                { override() },
                parameters = { "text" of "String" }) {
                "config".e.call("expand", "text".e, "context".e)
            }
            addSingleExprFunction(
                "accepts",
                { override() },
                parameters = { "editor" of "Editor".t.parameterizedBy { star() } }) {
                "editor".e instanceOf commonInterface.t.parameterizedBy { star() }
            }
        }
        writeToFile(pkg, simpleName, file)
    }

    private companion object {
        inline fun <E, reified F> List<E>.mapToArray(f: (E) -> F) = Array(size) { idx -> f(get(idx)) }
    }
}