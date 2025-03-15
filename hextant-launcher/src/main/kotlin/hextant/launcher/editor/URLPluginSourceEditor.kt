/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.codegen.ProvideFeature
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.launcher.plugins.PluginSource
import java.net.MalformedURLException
import java.net.URL

@ProvideFeature
class URLPluginSourceEditor() : TokenEditor<PluginSource?, TokenEditorView>() {
    constructor(text: String): this() {
        setInitialText(text)
    }

    override fun compile(token: String): PluginSource? = try {
        PluginSource.GitRepo(URL(token))
    } catch (e: MalformedURLException) {
        null
    }
}