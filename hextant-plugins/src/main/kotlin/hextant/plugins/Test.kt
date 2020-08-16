package hextant.plugins

import bundles.createBundle
import hextant.context.Context
import hextant.main.HextantApplication
import hextant.plugins.client.HttpPluginClient
import hextant.plugins.editor.PluginsEditor
import hextant.plugins.view.PluginsEditorControl
import javafx.scene.Parent
import java.io.File

class Test : HextantApplication() {
    override fun createView(context: Context): Parent {
        val url = "http://localhost:80"
        val downloadDirectory = File("D:/data/hextant/plugin-cache")
        val marketplace = HttpPluginClient(url, downloadDirectory)
        val types = Plugin.Type.values().toSet()
        val plugins = PluginManager(marketplace)
        val e = PluginsEditor(context, plugins, marketplace, types)
        return PluginsEditorControl(e, createBundle())
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch<Test>()
        }
    }
}