/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.codegen.ProvideFeature
import hextant.context.Context
import hextant.core.editor.*
import hextant.launcher.PluginSource
import validated.reaktive.ReactiveValidated

@ProvideFeature
class MavenPluginSourceEditor(context: Context) : CompoundEditor<PluginSource>(context) {
    val group by child(SimpleStringEditor(context))
    val artifact by child(SimpleStringEditor(context))

    override val result: ReactiveValidated<PluginSource.MavenCoordinate> = composeResult(group, artifact)
}