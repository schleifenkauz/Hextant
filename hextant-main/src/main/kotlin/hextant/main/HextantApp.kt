/**
 *@author Nikolaus Knop
 */

package hextant.main

import bundles.publicProperty
import bundles.set
import hextant.cli.HextantDirectory
import hextant.cli.fail
import hextant.cli.verifyFile
import hextant.command.line.CommandLinePopup
import hextant.context.Context
import hextant.context.Properties
import hextant.context.Properties.marketplace
import hextant.fx.*
import hextant.plugins.*
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import java.io.IOException

class HextantApp : Application() {
    private lateinit var context: Context

    override fun start(primaryStage: Stage) {
        setupContext()
        context[mainWindow] = primaryStage
        val project = processParameters()
        context[Project] = project
        project.setRootFile()
        if (PluginSource.dynamic()) project.listenForPluginChanges()
        showProject(project)
    }

    private fun setupContext() {
        PluginSource.fromParameters(parameters)
        if (PluginSource.dynamic()) javaClass.classLoader.addPluginsToClasspath(listOf("core"))
        bundles.runtimeTypeSafety = false
        context = Context.create {
            set(marketplace, LocalPluginRepository(HextantDirectory.resolve("plugins")))
        }
        Properties.setupContext(context)
        registerImplementations(listOf("main"), context)
        HextantMain.apply(context, PluginBuilder.Phase.Initialize, null)
    }

    private fun registerGlobalCommandLine(context: Context, project: Project) {
        val stage = context[mainWindow]
        val globalCL = context[Properties.globalCommandLine]
        val popup = CommandLinePopup(context, globalCL)
        project.view.registerShortcuts {
            handleCommands(context, context, globalCL)
            on("Ctrl?+G") {
                popup.show(stage)
            }
        }
    }

    private fun showProject(project: Project) {
        registerGlobalCommandLine(context, project)
        val stage = context[mainWindow]
        val icon = javaClass.getResource("icon_transparent.png")!!
        stage.icons.add(Image(icon.toExternalForm()))
        stage.scene = Scene(project.view)
        stage.scene.initHextantScene(context)
        stage.setOnShown {
            project.view.receiveFocusLater()
        }
        stage.show()
        runFXWithTimeout {
            setSize(stage)
            stage.title = "Hextant - ${project.name}"
        }
    }

    private fun setSize(stage: Stage) {
        when (val s = context[WindowSize]) {
            WindowSize.Maximized -> stage.isMaximized = true
            WindowSize.FullScreen -> stage.isFullScreen = true
            WindowSize.Default -> {
                stage.width = stage.scene.width
                stage.height = stage.scene.height
            }
            WindowSize.FitContent -> {
                stage.width = stage.scene.root.prefWidth(-1.0)
                stage.height = stage.scene.root.prefHeight(-1.0)
            }
            is WindowSize.Configured -> {
                stage.width = s.width
                stage.height = s.height
            }
        }
    }

    private fun processParameters(): Project {
        if (parameters.unnamed.size > 1) fail("Illegal number of arguments")
        val path = parameters.unnamed.firstOrNull()
            ?: HextantDirectory.resolve("launcher").absolutePath
            ?: fail("No default launcher configured")
        return if ("create" in parameters.named) {
            val projectTypeName = parameters.named.getValue("create")
            val project = Project.create(context, projectTypeName, path.verifyFile())
            if (parameters.named["save"] != "false") project.save()
            project
        } else {
            Project.open(context, path.verifyFile())
        }
    }

    companion object {
        val mainWindow by lazy { publicProperty<Stage>("stage") }

        fun launch(vararg args: String) {
            System.err.println(args.joinToString(" "))
            try {
                launch(HextantApp::class.java, *args)
            } catch (io: IOException) {
                io.printStackTrace()
                fail("Unexpected IO error: ${io.message}")
            } catch (ex: Exception) {
                ex.printStackTrace()
                fail("Unexpected exception: ${ex.message}")
            } catch (err: Error) {
                err.printStackTrace()
                fail("Unexpected error: ${err.message}")
            }
        }
    }
}