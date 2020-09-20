/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import bundles.SimpleProperty
import hextant.sample.Identifier
import reaktive.Observer
import reaktive.map.bindings.get
import reaktive.map.reactiveMap
import reaktive.value.*
import reaktive.value.binding.flatMap
import validated.*
import validated.reaktive.ReactiveValidated
import kotlin.collections.set

class GlobalScope {
    private val defs = reactiveMap<Identifier, FunctionDefinitionEditor>()

    fun addDefinition(editor: FunctionDefinitionEditor): Observer {
        editor.name.result.now.ifValid { n -> defs.now[n] = editor }
        return editor.name.result.observe { _, old, new ->
            old.ifValid { n -> defs.now.remove(n) }
            new.ifValid { n -> defs.now[n] = editor }
        }
    }

    fun removeDefinition(editor: FunctionDefinitionEditor) {
        editor.name.result.now.ifValid { n -> defs.now.remove(n) }
    }

    fun getDefinition(name: ReactiveValidated<Identifier>): ReactiveValue<FunctionDefinitionEditor?> =
        name.flatMap { n -> n.map { defs[it] }.ifInvalid { reactiveValue(null) } }

    val definitions: Collection<GlobalFunction>
        get() = defs.now.values.map { e ->
            GlobalFunction(
                e.returnType.result.now.orNull(),
                e.name.result.now.force(),
                e.parameters.results.now.map { it.orNull() }
            )
        }

    companion object : SimpleProperty<GlobalScope>("global scope")
}