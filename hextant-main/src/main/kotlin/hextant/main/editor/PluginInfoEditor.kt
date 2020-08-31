/**
 *@author Nikolaus Knop
 */

package hextant.main.editor

import hextant.context.Context
import hextant.core.editor.CompletionTokenEditor
import hextant.plugins.PluginInfo

internal abstract class PluginInfoEditor(context: Context) : CompletionTokenEditor<PluginInfo>(context)