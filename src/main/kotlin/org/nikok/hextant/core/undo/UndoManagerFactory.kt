/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.undo

import org.nikok.hextant.bundle.Property
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public

class UndoManagerFactory {
    private val managers = mutableMapOf<Any, UndoManager>()

    fun get(context: Any) = managers.getOrPut(context) { UndoManagerImpl() }

    companion object : Property<UndoManagerFactory, Public, Internal>("undo manager factory")
}