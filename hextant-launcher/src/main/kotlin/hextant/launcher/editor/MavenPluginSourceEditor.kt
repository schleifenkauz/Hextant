/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.codegen.ProvideFeature
import hextant.core.editor.CompoundEditor
import hextant.core.editor.SimpleStringEditor
import hextant.launcher.plugins.PluginSource
import hextant.launcher.plugins.PluginSource.MavenCoordinate
import reaktive.value.ReactiveValue

@ProvideFeature
class MavenPluginSourceEditor : CompoundEditor<PluginSource?>() {
    val group by child(SimpleStringEditor())
    val artifact by child(SimpleStringEditor())

    override val result: ReactiveValue<PluginSource?> = composeResult { MavenCoordinate(group.now, artifact.now) }
}