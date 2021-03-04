/**
 *@author Nikolaus Knop
 */

package hextant.plugins.editor

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.plugins.PluginInfo

internal abstract class PluginInfoEditor(context: Context) : TokenEditor<PluginInfo, TokenEditorView>(context)