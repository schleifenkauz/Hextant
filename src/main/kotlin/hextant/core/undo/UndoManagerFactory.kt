/**
 *@author Nikolaus Knop
 */

package hextant.core.undo

import hextant.bundle.Property
import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public

class UndoManagerFactory {
    private val managers = mutableMapOf<Any, UndoManager>()

    fun get(context: Any) = managers.getOrPut(context) { UndoManagerImpl() }

    companion object : Property<UndoManagerFactory, Public, Internal>("undo manager factory")
}