/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.codegen.ProvideFeature
import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.core.editor.SimpleStringEditor
import hextant.launcher.plugins.PluginSource
import hextant.launcher.plugins.PluginSource.MavenCoordinate
import reaktive.value.ReactiveValue

@ProvideFeature
class MavenPluginSourceEditor(context: Context) : CompoundEditor<PluginSource?>(context) {
    val group by child(SimpleStringEditor(context))
    val artifact by child(SimpleStringEditor(context))

    override val result: ReactiveValue<PluginSource?> = composeResult { MavenCoordinate(group.now, artifact.now) }
}