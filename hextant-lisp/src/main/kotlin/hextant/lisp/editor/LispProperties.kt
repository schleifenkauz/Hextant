/**
 * @author Nikolaus Knop
 */

package hextant.lisp.editor


import hextant.bundle.SimpleProperty
import hextant.lisp.FileScope

object LispProperties {
    sealed class Internal {
        internal companion object : Internal()
    }

    val fileScope = SimpleProperty<FileScope>("file scope")
}