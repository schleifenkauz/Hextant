/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.codegen.ProvideFeature
import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.core.editor.SimpleStringEditor
import hextant.launcher.PluginSource
import validated.reaktive.ReactiveValidated
import validated.reaktive.composeReactive
import java.net.URL

@ProvideFeature
class GitHubPluginSourceEditor(context: Context) : CompoundEditor<PluginSource>(context) {
    val userName by child(SimpleStringEditor(context))
    val repository by child(SimpleStringEditor(context))

    override val result: ReactiveValidated<PluginSource> =
        composeReactive(userName.result, repository.result) { user, repo ->
            PluginSource.GitRepo(URL("https://github.com/$user/$repo"))
        }
}