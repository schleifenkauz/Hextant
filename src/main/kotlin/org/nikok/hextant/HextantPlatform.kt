/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.EditableFactory
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.command.Commands
import org.nikok.hextant.core.impl.SelectionDistributor
import org.nikok.hextant.core.inspect.Inspections
import org.nikok.hextant.prop.PropertyHolder
import org.nikok.hextant.prop.invoke

/**
 * The hextant platform, mainly functions as a [PropertyHolder] to manage properties of the hextant platform
*/
interface HextantPlatform : PropertyHolder {
    companion object : HextantPlatform, PropertyHolder by PropertyHolder.newInstance() {
        init {
            Internal {
                HextantPlatform[Version] = Version(1, 0, isSnapshot = true)
                HextantPlatform[SelectionDistributor] = SelectionDistributor.newInstance()
                HextantPlatform[EditorViewFactory] = EditorViewFactory.newInstance()
                HextantPlatform[EditableFactory] = EditableFactory.newInstance()
                HextantPlatform[Commands] = Commands.newInstance()
                HextantPlatform[Inspections] = Inspections.newInstance()
            }
        }

    }
}
