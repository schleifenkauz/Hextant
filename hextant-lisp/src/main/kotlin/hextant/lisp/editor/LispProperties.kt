/**
 * @author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.bundle.CorePermissions.Public
import hextant.bundle.Permission
import hextant.bundle.Property
import hextant.lisp.FileScope

object LispProperties {
    sealed class Internal : Permission() {
        internal companion object : Internal()
    }

    val fileScope = Property<FileScope, Public, Public>("file scope")
}