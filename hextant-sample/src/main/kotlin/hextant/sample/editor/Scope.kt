/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import bundles.SimpleProperty
import hextant.sample.Identifier
import hextant.sample.SimpleType
import reaktive.Observer
import reaktive.list.MutableReactiveList
import reaktive.list.binding.first
import reaktive.list.reactiveList
import reaktive.value.*
import reaktive.value.binding.flatMap
import reaktive.value.binding.orElse
import validated.*
import validated.Validated.Valid
import validated.reaktive.ReactiveValidated

class Scope private constructor(private val parent: Scope?) {
    private val definitions = mutableMapOf<Identifier, MutableReactiveList<SimpleType>>()

    fun resolve(name: ReactiveValidated<Identifier>, line: ReactiveInt): ReactiveValue<SimpleType?> {
        val t = name.flatMap { r ->
            r.map { n ->
                definitions.getOrPut(n) { reactiveList() }.first()
            }.ifInvalid { reactiveValue(null) }
        }
        return if (parent == null) t
        else t.orElse(parent.resolve(name, line))
    }

    fun addDefinition(
        name: ReactiveValidated<Identifier>,
        line: ReactiveInt,
        type: ReactiveValidated<SimpleType>
    ): Observer {
        addDefinition(name.now, type.now, line.now)
        return name.observe { _, old, new ->
            removeDefinition(old, type.now, line.now)
            addDefinition(new, type.now, line.now)
        } and type.observe { _, old, new ->
            removeDefinition(name.now, old, line.now)
            addDefinition(name.now, new, line.now)
        }
    }

    fun availableBindings(): List<Binding> = definitions.entries.mapNotNull { (name, types) ->
        if (types.now.isNotEmpty()) Binding(name, types.now.first()) else null
    } + parent?.availableBindings().orEmpty()

    private fun removeDefinition(name: Validated<Identifier>, type: Validated<SimpleType>, line: Int) {
        if (name is Valid && type is Valid) {
            val removed = definitions.getOrElse(name.value) { reactiveList() }.now.remove(type.value)
            check(removed) { "Could not remove definition ($name = $type)" }
        }
    }

    private fun addDefinition(name: Validated<Identifier>, type: Validated<SimpleType>, line: Int) {
        if (name is Valid && type is Valid) {
            definitions.getOrPut(name.value) { reactiveList() }.now.add(type.value)
        }
    }

    fun undefine(name: Validated<Identifier>, type: Validated<SimpleType>, line: Int) {
        removeDefinition(name, type, line)
    }

    fun child() = Scope(parent = this)

    data class Binding(val name: Identifier, val type: SimpleType)

    companion object : SimpleProperty<Scope>("scope") {
        fun root() = Scope(parent = null)
    }
}