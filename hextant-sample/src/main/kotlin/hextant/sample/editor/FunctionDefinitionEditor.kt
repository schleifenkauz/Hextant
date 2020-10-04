package hextant.sample.editor

import hextant.codegen.ProvideFeature
import hextant.codegen.ProvideImplementation
import hextant.context.Context
import hextant.context.EditorFactory
import hextant.core.Editor
import hextant.core.editor.CompoundEditor
import hextant.sample.FunctionDefinition
import reaktive.Observer
import reaktive.list.observeEach
import reaktive.value.now
import reaktive.value.reactiveValue
import validated.ifInvalid
import validated.map
import validated.reaktive.ReactiveValidated
import validated.reaktive.composeReactive

@ProvideFeature
class FunctionDefinitionEditor @ProvideImplementation(EditorFactory::class) constructor(context: Context) :
    CompoundEditor<FunctionDefinition>(context) {
    val returnType by child(SimpleTypeEditor(context))
    val name by child(IdentifierEditor(context))
    val parameters by child(ParameterListEditor(context))
    val body by child(BlockEditor(context))

    private val scopeManagement: Observer
    private val scope = body.statements.context[Scope]

    init {
        val line = reactiveValue(-1)
        scopeManagement = parameters.editors.observeEach { _, p ->
            val name = p.name.result
            val type = p.type.result
            scope.addDefinition(name, line, type)
        } and parameters.editors.observeList { ch ->
            if (ch.wasRemoved) {
                val name = ch.added.name.result.now
                val type = ch.added.type.result.now
                scope.removeDefinition(name, type, line)
            }
        }
    }

    override fun toString(): String = buildString {
        append(displayResult(returnType))
        append(' ')
        append(displayResult(name))
        parameters.editors.now.joinTo(this, separator = ", ", prefix = "(", postfix = ")") { e -> displayResult(e) }
    }

    private fun displayResult(subEditor: Editor<*>) =
        subEditor.result.now.map { it.toString() }.ifInvalid { "<invalid>" }

    override val result: ReactiveValidated<FunctionDefinition> =
        composeReactive(returnType.result, name.result, parameters.result, body.result, ::FunctionDefinition)
}
