/**
 *@author Nikolaus Knop
 */

package hextant.main.editor

import hextant.context.Context
import hextant.plugins.PluginInfo.Type

internal class DisabledPluginInfoEditor(context: Context, val types: Set<Type>) : PluginInfoEditor(context)