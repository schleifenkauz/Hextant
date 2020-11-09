/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.codegen.ProvideFeature
import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.launcher.PluginSource
import validated.invalid
import validated.valid
import java.net.MalformedURLException
import java.net.URL

@ProvideFeature
class URLPluginSourceEditor(context: Context, text: String = "") :
    TokenEditor<PluginSource, TokenEditorView>(context, text) {
    override fun wrap(token: String): PluginSource = try {
        valid(PluginSource.GitRepo(URL(token)))
    } catch (e: MalformedURLException) {
        invalid("Malformed URL '$token'")
    }
}