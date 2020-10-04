/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import bundles.SimpleProperty
import hextant.sample.Identifier
import hextant.sample.SimpleType
import reaktive.Observer
import reaktive.collection.binding.find
import reaktive.dependencies
import reaktive.set.*
import reaktive.value.*
import reaktive.value.binding.*
import validated.*
import validated.Validated.Valid
import validated.reaktive.ReactiveValidated

class Scope private constructor(private val parent: Scope?) {
    private val definitions = mutableMapOf<Identifier, MutableReactiveSet<Def>>()

    private fun definitions(name: Identifier) =
        definitions.getOrPut(name) { reactiveSet() }

    fun resolve(name: ReactiveValidated<Identifier>, line: ReactiveInt): ReactiveValue<SimpleType?> {
        val t = name.flatMap { r ->
            r.map { n ->
                definitions(n)
                    .withDependencies { dependencies(it.line, line) }
                    .find { it.line.now < line.now }
                    .map { it?.type }
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
        addDefinition(name.now, type.now, line)
        return name.observe { _, old, new ->
            removeDefinition(old, type.now, line)
            addDefinition(new, type.now, line)
        } and type.observe { _, old, new ->
            removeDefinition(name.now, old, line)
            addDefinition(name.now, new, line)
        }
    }

    fun availableBindings(line: Int): List<Def> =
        definitions.values.flatMap { it.now }.filter { it.line.now < line } + parent?.availableBindings(line).orEmpty()

    fun removeDefinition(name: Validated<Identifier>, type: Validated<SimpleType>, line: ReactiveInt) {
        if (name is Valid && type is Valid) {
            val def = Def(name.value, type.value, line)
            val removed = definitions(name.value).now.remove(def)
            check(removed) { "Could not remove $def because only ${definitions(name.value).now} exist" }
        }
    }

    private fun addDefinition(name: Validated<Identifier>, type: Validated<SimpleType>, line: ReactiveInt) {
        if (name is Valid && type is Valid) {
            definitions(name.value).now.add(Def(name.value, type.value, line))
        }
    }

    fun child() = Scope(parent = this)

    data class Def(val name: Identifier, val type: SimpleType, val line: ReactiveInt) {
        override fun equals(other: Any?): Boolean = when {
            this === other                  -> true
            other !is Def                   -> false
            this.name != other.name         -> false
            this.type != other.type         -> false
            this.line.now != other.line.now -> false
            else                            -> true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + type.hashCode()
            return result
        }

        override fun toString(): String = "$type $name on line ${line.now}"
    }

    companion object : SimpleProperty<Scope>("scope") {
        fun root() = Scope(parent = null)
    }
}