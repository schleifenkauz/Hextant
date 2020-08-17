/**
 *@author Nikolaus Knop
 */

package hextant.main

import Core
import bundles.SimpleProperty
import hextant.command.line.CommandLine
import hextant.command.line.SingleCommandSource
import hextant.command.meta.CommandParameter
import hextant.command.meta.ProvideCommand
import hextant.context.Context
import hextant.context.createView
import hextant.fx.*
import hextant.main.editors.ProjectLocationEditor
import hextant.plugins.LocatedProjectType
import hextant.plugins.Marketplace
import javafx.geometry.Pos.CENTER
import javafx.scene.text.Font
import java.io.File

internal class HextantApp : HextantApplication() {
    private val settings = File(System.getProperty("user.home")).resolve(".hextant")

    private val recent = recentProjects()

    private fun recentProjects(): MutableSet<File> {
        val file = settings.resolve(RECENT_PROJECTS)
        return if (!file.exists()) mutableSetOf()
        else file.readLines().mapTo(mutableSetOf()) { File(it) }
    }

    override fun createContext(root: Context): Context {
        Core.apply(root)
        Main.apply(root)
        return super.createContext(root)
    }

    override fun createView(context: Context) = vbox {
        recent.add(File("D:/dev/hextant/project.hxt"))
        context[recentProjects] = recent
        //        context[marketplace] = HttpPluginClient("http://localhost:80", settings.resolve("plugin-cache"))
        context[marketplace] = MarketplaceMock
        setPrefSize(400.0, 400.0)
        alignment = CENTER
        spacing = 30.0
        add(label("Hextant")) {
            font = Font(24.0)
        }
        val src = SingleCommandSource(context, this@HextantApp)
        val cl = CommandLine(context, src)
        val view = context.createView(cl)
        add(view)
    }

    @ProvideCommand("Create New Project", "create", defaultShortcut = "Ctrl+N")
    fun createNewProject(projectType: LocatedProjectType) {

    }

    @ProvideCommand("Open Project", "open", defaultShortcut = "Ctrl+O")
    fun openProject(
        @CommandParameter(
            description = "The opened project",
            editWith = ProjectLocationEditor::class
        ) project: File
    ) {

    }

    override fun stop() {
        val f = settings.resolve(RECENT_PROJECTS)
        val w = f.bufferedWriter()
        val lines = recent.joinToString("\n")
        w.write(lines)
    }

    companion object {
        val recentProjects = SimpleProperty<Collection<File>>("recent projects")

        val marketplace = SimpleProperty<Marketplace>("marketplace")

        @JvmStatic
        fun main(args: Array<String>) {
            launch(HextantApp::class.java, *args)
        }

        private const val RECENT_PROJECTS = "recent.txt"
    }
}