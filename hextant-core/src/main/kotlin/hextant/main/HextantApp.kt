/**
 *@author Nikolaus Knop
 */

package hextant.main

import bundles.SimpleProperty
import hextant.command.line.CommandLine
import hextant.command.line.SingleCommandSource
import hextant.context.Context
import hextant.context.HextantPlatform.defaultContext
import hextant.context.HextantPlatform.projectContext
import hextant.context.createControl
import hextant.core.Core
import hextant.core.view.EditorControl
import hextant.fx.*
import hextant.plugin.Aspects
import hextant.plugin.PluginBuilder.Phase.Initialize
import hextant.plugins.Implementation
import hextant.plugins.Marketplace
import hextant.plugins.client.HttpPluginClient
import javafx.application.Application
import javafx.geometry.Pos.CENTER
import javafx.scene.Scene
import javafx.scene.text.Font
import javafx.stage.Stage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

internal class HextantApp : Application() {
    private val globalContext = Context.newInstance {
        set(marketplace, HttpPluginClient("http://localhost:80", pluginCache))
    }

    private val projectContext = projectContext(globalContext)
    private val localContext = defaultContext(projectContext)

    override fun start(primaryStage: Stage) {
        globalContext[stage] = primaryStage
        initCore()
        val scene = Scene(createView())
        scene.initHextantScene(localContext)
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun initCore() {
        val url = javaClass.classLoader.getResource("implementations.json")!!
        val desc = url.openStream().bufferedReader().readText()
        val impls = Json.decodeFromString<List<Implementation>>(desc)
        for (impl in impls) {
            localContext[Aspects].addImplementation(impl)
        }
        Core.apply(projectContext, Initialize, project = null)
    }

    private fun createView() = hbox {
        setPrefSize(400.0, 400.0)
        alignment = CENTER
        add(vbox()) {
            setPrefSize(200.0, 400.0)
            alignment = CENTER
            spacing = 30.0
            add(label("Hextant")) {
                font = Font(24.0)
            }
            add(createCommandLine())
        }
    }

    private fun createCommandLine(): EditorControl<*> {
        val receiver = ProjectManager(localContext)
        val src = SingleCommandSource(localContext, receiver)
        val cl = CommandLine(localContext, src)
        return localContext.createControl(cl)
    }

    companion object {
        val marketplace = SimpleProperty<Marketplace>("marketplace")

        val globalDir = File(System.getProperty("user.home")).resolve("hextant")

        val stage = SimpleProperty<Stage>("stage")

        val projects = globalDir.resolve("projects")

        val pluginCache = globalDir.resolve("plugin-cache")
    }
}