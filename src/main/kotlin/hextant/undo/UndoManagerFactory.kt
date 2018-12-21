/**
 *@author Nikolaus Knop
 */

package hextant.undo

import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property

class UndoManagerFactory {
    private val managers = mutableMapOf<Any, UndoManager>()

    fun get(context: Any) = managers.getOrPut(context) { UndoManagerImpl() }

    companion object : Property<UndoManagerFactory, Public, Internal>("undo manager factory")
}