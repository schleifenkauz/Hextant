/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.undo

import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.prop.Property

class UndoManagerFactory {
    private val managers = mutableMapOf<Any, UndoManager>()

    fun get(context: Any) = managers.getOrPut(context) { UndoManagerImpl() }

    companion object : Property<UndoManagerFactory, Public, Internal>("undo manager factory")
}