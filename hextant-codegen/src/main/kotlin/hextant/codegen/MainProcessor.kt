/**
 *@author Nikolaus Knop
 */

package hextant.codegen

import hextant.codegen.aspects.AspectCollector
import hextant.codegen.aspects.FeatureCollector
import hextant.codegen.aspects.ImplementationCollector
import hextant.codegen.aspects.ProjectTypeCollector
import hextant.codegen.editor.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

internal class MainProcessor : AbstractProcessor() {
    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(
        fqName<Token>(),
        fqName<Compound>(),
        fqName<NodeType>(),
        fqName<EditorInterface>(),
        fqName<Expandable>(),
        fqName<Choice>(),
        fqName<ListEditor>(),
        fqName<RegisterEditor>(),
        fqName<RequestAspect>(),
        fqName<ProvideFeature>(),
        fqName<ProvideImplementation>(),
        fqName<ProvideProjectType>(),
    )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedOptions(): MutableSet<String> = mutableSetOf(GENERATED_DIR, ACCESSOR_PACKAGE)

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isEmpty()) return false
        for (proc in processors) proc.init(processingEnv)
        for (proc in editorCodegen) proc.preprocess(roundEnv)
        for (proc in editorCodegen) proc.process(roundEnv)
        for (proc in collectors) proc.process(roundEnv)
        EditorClassGen.reprocessGenerated()
        for (proc in collectors) proc.finish()
        return true
    }

    companion object {
        const val GENERATED_DIR = "kapt.kotlin.generated"
        const val ACCESSOR_PACKAGE = "hextant.aspect.accessor.pkg"

        private val editorCodegen = listOf(
            EditorRegistrar,
            TokenEditorCodegen,
            CompoundEditorCodegen,
            AlternativeInterfaceCodegen,
            ExpanderClassGen,
            ListEditorCodegen,
            ChoiceEditorCodegen
        )

        private val collectors = listOf(
            AspectCollector,
            FeatureCollector,
            ImplementationCollector,
            ProjectTypeCollector
        )

        private val processors = editorCodegen + collectors
    }
}