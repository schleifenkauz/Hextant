/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.hextant.prop.PropertyHolder

/**
 * The hextant platform, mainly functions as a [PropertyHolder] to manage properties of the hextant platform
*/
interface HextantPlatform : PropertyHolder {
    companion object : HextantPlatform, PropertyHolder by PropertyHolder.newInstance()
}
