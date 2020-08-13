/**
 * @author Nikolaus Knop
 */

package hextant.codegen

import com.google.auto.service.AutoService
import hextant.context.Context
import hextant.context.EditorFactory
import hextant.core.Editor
import hextant.core.editor.*
import hextant.core.view.TokenEditorView
import krobot.api.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.*
import kotlin.reflect.KClass

@Suppress("unused")
@AutoService(Processor::class)
class EditorCodegen : AbstractProcessor() {
    private lateinit var generatedDir: String

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(Token::class.qualifiedName!!)

    private fun getOneAnnotation(
        element: Element,
        annotationClasses: Set<KClass<out Annotation>>
    ): Annotation {
        val annotations = annotationClasses.mapNotNull { element.getAnnotation(it.java) }
        ensure(annotations.isNotEmpty()) { "$element is not annotated with any editor codegen annotation" }
        ensure(annotations.size <= 1) { "$element is annotated with more than one editor codegen annotation" }
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
        processingEnv.executeSafely {
            generatedDir = processingEnv.options["kapt.kotlin.generated"]!!
            with(roundEnv) {
                processAnnotations(::genTokenEditorClass)
                processAnnotations(::genCompoundEditorClass)
                processAnnotations(::genAlternativeInterface)
                processAnnotations(::genExpanderClass)
                processAnnotations(::genListEditorClass)
            }
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
        writeToFile(generatedDir, pkg, name, file)
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
                    "TokenEditor".t.parameterizedBy { invariant(name); invariant("TokenEditorView") },
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
        writeToFile(generatedDir, pkg, simpleName, file)
    }

    private fun KInheritanceRobot.implementEditorOfSuperType(
        annotation: Annotation,
        simpleName: String
    ) {
        val supertype = getTypeMirror { annotation.subtypeOf }.toString()
        if (supertype != None::class.qualifiedName) {
            val el = processingEnv.elementUtils.getTypeElement(supertype)
            val ann = el.getAnnotation(Alternative::class.java)
            if (ann == null) fail("No annotation of type Alternative on $supertype")
            else {
                val editorQN = extractQualifiedEditorClassName(ann, el)
                implement(editorQN.t.parameterizedBy { invariant(simpleName) })
            }
        }
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


    private fun genCompoundEditorClass(annotated: TypeElement, annotation: Compound) {
        val name = annotated.simpleName.toString()
        val qn = extractQualifiedEditorClassName(annotation, annotated)
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val members = processingEnv.elementUtils.getAllMembers(annotated)
        val constructors = members.filter {
            it.simpleName.toString() == "<init>"
        }
        ensure(constructors.size == 1) { "Class $annotated with annotation @Compound has ${constructors.size} constructors" }
        val primary = constructors[0] as ExecutableElement
        val file = kotlinClass(
            pkg,
            imports = {
                import<CompoundEditor<*>>()
                import(annotated.toString())
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
                val editorCls = getEditorClassName(p.asType(), p)
                addVal(p.simpleName.toString()) {
                    by(call("child", call(editorCls, "context".e)))
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
        writeToFile(generatedDir, pkg, simpleName, file)
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
        fail("Invalid component type $t(${t::class})")
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
            typeParameters = { covariant(typeParam, upperBound = name.t) },
            inheritance = { implement("Editor".t.parameterizedBy { invariant(typeParam) }) }
        )
        writeToFile(generatedDir, pkg, simpleName, file)
    }

    private fun genExpanderClass(annotated: Element, annotation: Expandable) {
        val name = annotated.simpleName.toString()
        val qn = extractQualifiedEditorClassName(annotation, annotated, classNameSuffix = "Expander")
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val ann = annotated.getAnnotation(Alternative::class.java)
        val commonInterface = extractQualifiedEditorClassName(ann, annotated)
        val editorType = commonInterface.t.parameterizedBy { invariant(name) }
        val delegator = getTypeMirror(annotation::delegator)
        val file = kotlinClass(
            pkg, {
                import("hextant.context.*")
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
                        invariant(name)
                        invariant(editorType)
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
        }
        writeToFile(generatedDir, pkg, simpleName, file)
    }

    private companion object {
        val Annotation.qualifiedEditorClassName: String?
            get() = when (this) {
                is Token -> this.classLocation.takeIf { it != DEFAULT }
                is Compound -> this.classLocation.takeIf { it != DEFAULT }
                is Alternative -> this.interfaceLocation.takeIf { it != DEFAULT }
                is Expandable -> this.expanderLocation.takeIf { it != DEFAULT }
                is EditableList -> this.classLocation.takeIf { it != DEFAULT }
                else            -> throw AssertionError()
            }

        val Annotation.subtypeOf: KClass<*>
            get() = when (this) {
                is Token -> this.subtypeOf
                is Compound -> this.subtypeOf
                is Expandable -> this.subtypeOf
                else          -> throw AssertionError()
            }
    }
}