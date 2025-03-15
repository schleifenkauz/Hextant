/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.codegen.ProvideFeature
import hextant.codegen.ProvideImplementation
import hextant.context.EditorFactory
import hextant.core.Editor
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderConfig
import hextant.launcher.plugins.PluginSource

@ProvideFeature
class PluginSourceExpander @ProvideImplementation(EditorFactory::class) constructor() :
    ConfiguredExpander<PluginSource?, Editor<PluginSource?>>() {
    init {
        configure(config)
    }

    companion object {
        val config = ExpanderConfig<Editor<PluginSource?>>().apply {
            "http" expand { URLPluginSourceEditor("http://") }
            "https" expand { URLPluginSourceEditor("https://") }
            "github" expand { GitHubPluginSourceEditor() }
        }
    }
}