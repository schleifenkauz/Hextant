package hextant.sample.editor

import hextant.codegen.ProvideFeature
import hextant.codegen.ProvideImplementation
import hextant.context.*
import hextant.core.editor.CompoundEditor
import hextant.sample.Block
import hextant.sample.Statement
import reaktive.Observer
import reaktive.list.observeEach
import reaktive.value.now
import validated.reaktive.ReactiveValidated
import validated.reaktive.composeReactive

@ProvideFeature
class BlockEditor @ProvideImplementation(EditorFactory::class) constructor(context: Context) :
    CompoundEditor<Block>(context), StatementEditor<Block> {
    private val scope = context[Scope].child()

    val statements by child(StatementListEditor(context.extend { set(Scope, scope) }))

    private val scopeManagement: Observer
    private val observers = mutableMapOf<DefinitionEditor, Observer>()

    init {
        scopeManagement = statements.editors.observeEach { _, e ->
            addStatement(e.editor.now)
            e.editor.observe { _, old, new ->
                addStatement(new)
                removeStatement(old)
            }
        } and statements.editors.observeList { ch ->
            if (ch.wasRemoved) removeStatement(ch.removed.editor.now)
        }
    }

    private fun removeStatement(old: StatementEditor<Statement>?) {
        if (old is DefinitionEditor) {
            observers.remove(old)!!.kill()
            scope.removeDefinition(old.name.result.now, old.type.result.now, old.line)
        }
    }

    private fun addStatement(new: StatementEditor<Statement>?) {
        if (new is DefinitionEditor) {
            observers[new] = scope.addDefinition(new.name.result, new.line, new.type.result)
        }
    }

    override val result: ReactiveValidated<Block> = composeReactive(statements.result, ::Block)
}
