/**
 *@author Nikolaus Knop
 */

package hextant.lisp

import hextant.lisp.SinglyLinkedList.Cons
import hextant.lisp.SinglyLinkedList.Empty

sealed class SinglyLinkedList<out T> : Iterable<T> {
    abstract val head: T

    abstract val tail: SinglyLinkedList<T>

    override fun iterator(): Iterator<T> = Itr(this)

    class Itr<T>(list: SinglyLinkedList<T>) : Iterator<T> {
        private var current = list

        override fun hasNext(): Boolean = current != Empty

        override fun next(): T {
            val el = current.head
            current = current.tail
            return el
        }
    }

    data class Cons<out T>(override val head: T, override val tail: SinglyLinkedList<T>) : SinglyLinkedList<T>()

    object Empty : SinglyLinkedList<Nothing>() {
        override val head: Nothing
            get() = throw LispRuntimeError("List is empty")

        override val tail: SinglyLinkedList<Nothing>
            get() = throw LispRuntimeError("List is empty")
    }

    companion object {
        fun <T> empty(): SinglyLinkedList<T> = Empty

        fun <T> fromList(list: List<T>): SinglyLinkedList<T> =
            list.asReversed().fold(empty()) { lst, e -> Cons(e, lst) }
    }
}

fun <T, F> SinglyLinkedList<T>.map(f: (T) -> F): SinglyLinkedList<F> = when (this) {
    is Empty -> Empty
    is Cons  -> Cons(f(head), tail.map(f))
}