/**
 *@author Nikolaus Knop
 */

package hextant.main

import bundles.publicProperty
import bundles.set
import hextant.command.line.CommandLine
import hextant.command.line.CommandLinePopup
import hextant.command.line.SingleCommandSource
import hextant.context.*
import hextant.context.Properties.marketplace
import hextant.fx.*
import hextant.install.fail
import hextant.install.verifyFile
import hextant.main.HextantDirectory.Companion.PROJECT_ROOT
import hextant.plugins.*
import hextant.serial.PhysicalFile
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import java.io.IOException
import java.util.prefs.Preferences

class HextantApp : Application() {
    private lateinit var context: Context

    override fun start(primaryStage: Stage) {
        setupContext()
        context[mainWindow] = primaryStage
        val project = processParameters()
        project.setRootFile()
        if (PluginSource.dynamic()) project.listenForPluginChanges()
        showProject(project)
    }

    private fun setupContext() {
        PluginSource.fromParameters(parameters)
        val dir = HextantDirectory.get()
        if (PluginSource.dynamic()) javaClass.classLoader.addPluginsToClasspath(listOf("core"), dir)
        context = Context.create {
            set(HextantDirectory, dir)
            set(marketplace, LocalPluginRepository(dir[HextantDirectory.PLUGIN_CACHE]))
        }
        Properties.setupContext(context)
        registerImplementations(listOf("core", "main"), context)
    }

    private fun registerGlobalCommandLine(context: Context, project: Project) {
        val src = SingleCommandSource(context, context)
        val cl = CommandLine(context, src)
        val stage = context[mainWindow]
        val popup = CommandLinePopup(context, cl)
        project.view.registerShortcuts {
            handleCommands(context, context)
            on("Ctrl?+G") {
                popup.show(stage)
            }
        }
    }

    private fun showProject(project: Project) {
        context[Project] = project
        registerGlobalCommandLine(context, project)
        val stage = context[mainWindow]
        val icon = javaClass.getResource("icon.png")
        stage.icons.add(Image(icon.toExternalForm()))
        stage.scene = Scene(project.view)
        stage.scene.initHextantScene(context)
        stage.show()
        runFXWithTimeout {
            setSize(stage)
            stage.title = "Hextant - ${project.name}"
            project.view.receiveFocus()
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
            ?: Preferences.userRoot().get("launcher-path", null)
            ?: fail("No default launcher configured")
        return if ("create" in parameters.named) {
            val projectTypeClass = parameters.named.getValue("create")
            val project = Project.create(context, projectTypeClass, path.verifyFile())
            if (parameters.named["save"] != "false") project.save()
            project
        } else {
            Project.open(context, path.verifyFile())
        }
    }

    companion object {
        private val mainWindow = publicProperty<Stage>("stage")

        @JvmStatic
        fun main(vararg args: String) {
            bundles.runtimeTypeSafety = false
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