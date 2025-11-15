package hextant.expr

import fxutils.background
import hextant.context.Context
import hextant.context.Properties.classLoader
import hextant.context.createControl
import hextant.core.HextantCore
import hextant.expr.editor.ExpressionEditor
import hextant.fx.initHextantScene
import hextant.plugins.Aspects
import hextant.plugins.Implementation
import hextant.plugins.PluginBuilder
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlinx.serialization.json.Json
import kotlin.collections.iterator
import kotlin.system.exitProcess

class ExprApp : Application() {
    private lateinit var context: Context

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Expression Evaluator"
        context = HextantCore.defaultContext()
        val editor = ExpressionEditor()
        registerImplementationsFromClasspath()
        ExprPlugin.apply(context, PluginBuilder.Phase.Initialize, null)
        HextantCore.apply(context, PluginBuilder.Phase.Initialize, null)
        editor.initialize(context)
        val ctrl = context.createControl(editor)
        ctrl.background = background(Color.BLACK)
        primaryStage.scene = Scene(ctrl)
        primaryStage.scene.initHextantScene(context)
        primaryStage.show()
    }

    private fun registerImplementationsFromClasspath() {
        val cl = context[classLoader]
        for (impls in cl.getResources("implementations.json")) {
            val implementations: List<Implementation> = Json.decodeFromString(impls.readText())
            for (impl in implementations) {
                context[Aspects].addImplementation(impl, cl)
            }
        }
    }

    override fun stop() {
        super.stop()
        exitProcess(0)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(ExprApp::class.java, *args)
        }
    }
}