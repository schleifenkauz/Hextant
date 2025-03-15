/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.codegen.ProvideFeature
import hextant.core.editor.CompoundEditor
import hextant.core.editor.SimpleStringEditor
import hextant.launcher.plugins.PluginSource
import reaktive.value.ReactiveValue
import java.net.URL

@ProvideFeature
class GitHubPluginSourceEditor : CompoundEditor<PluginSource?>() {
    val userName by child(SimpleStringEditor())
    val repository by child(SimpleStringEditor())

    override val result: ReactiveValue<PluginSource?> = composeResult {
        PluginSource.GitRepo(URL("https://github.com/${userName.now}/${repository.now}"))
    }
}