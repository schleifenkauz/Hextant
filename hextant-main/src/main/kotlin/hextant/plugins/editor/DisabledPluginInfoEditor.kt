/**
 *@author Nikolaus Knop
 */

package hextant.plugins.editor

import hextant.plugins.PluginInfo
import hextant.plugins.PluginInfo.Type

internal class DisabledPluginInfoEditor(val types: Set<Type>) : PluginInfoEditor() {
    override fun compile(token: String): PluginInfo {
        TODO("Not yet implemented")
    }
}