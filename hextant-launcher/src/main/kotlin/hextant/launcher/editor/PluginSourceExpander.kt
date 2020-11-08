/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.codegen.ProvideFeature
import hextant.codegen.ProvideImplementation
import hextant.context.Context
import hextant.context.EditorFactory
import hextant.core.Editor
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderConfig
import hextant.launcher.PluginSource

@ProvideFeature
class PluginSourceExpander @ProvideImplementation(EditorFactory::class) constructor(context: Context) :
    ConfiguredExpander<PluginSource, Editor<PluginSource>>(config, context) {
    companion object {
        val config = ExpanderConfig<Editor<PluginSource>>().apply {
            "http" expand { ctx -> URLPluginSourceEditor(ctx, "http://") }
            "https" expand { ctx -> URLPluginSourceEditor(ctx, "https://") }
            "github" expand ::GitHubPluginSourceEditor
        }
    }
}