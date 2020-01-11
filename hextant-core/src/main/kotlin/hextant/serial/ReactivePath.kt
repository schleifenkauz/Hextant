/**
 *@author Nikolaus Knop
 */

package hextant.serial

import reaktive.value.*
import java.nio.file.Path
import java.nio.file.Paths

sealed class ReactivePath {
    override fun toString(): String = now.toString()

    companion object {
        fun empty(): ReactivePath = Nil

        fun fromPath(path: Path): ReactivePath = Resolve(Nil, reactiveValue(path.toString()))
    }
}

private object Nil : ReactivePath()

private data class Resolve(val parent: ReactivePath, val name: ReactiveString) : ReactivePath()

val ReactivePath.now: Path
    get() {
        require(this is Resolve) { "Empty path" }
        val components = mutableListOf<String>()
        var cur = this
        while (cur is Resolve) {
            components.add(cur.name.now)
            cur = cur.parent
        }
        val more = Array(components.size - 1) { idx -> components[components.size - 2 - idx] }
        return Paths.get(components.last(), *more)
    }

fun ReactivePath.resolve(name: ReactiveString): ReactivePath = Resolve(this, name)
