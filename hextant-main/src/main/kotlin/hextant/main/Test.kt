package hextant.main

import bundles.createBundle
import hextant.context.Context
import hextant.fx.menuBar
import hextant.main.editors.PluginsEditor
import hextant.main.editors.PluginsEditorControl
import hextant.main.plugins.PluginManager
import hextant.plugins.Marketplace
import hextant.plugins.Plugin.Type
import hextant.plugins.client.HttpPluginClient
import javafx.scene.Parent
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import java.io.File

class Test : HextantApplication() {
    override fun createView(context: Context): Parent {
        val url = "http://localhost:80"
        val downloadDirectory = File("D:/data/hextant/plugin-cache")
        val marketplace: Marketplace = HttpPluginClient(url, downloadDirectory)
        val types = Type.values().toSet()
        val plugins = PluginManager(marketplace)
        val e = PluginsEditor(context, plugins, marketplace, types)
        val fc = FileChooser()
        fc.extensionFilters.add(ExtensionFilter("Java Archives", "*.jar"))
        val menu = menuBar {
            menu("File") {
                item("Upload") {
                    val jar = fc.showOpenDialog(stage)
                    marketplace.upload(jar)
                }
            }
        }
        val c = PluginsEditorControl(e, createBundle())
        return VBox(menu, c)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch<Test>()
        }
    }
}