package hextant.test

import hextant.context.Context
import hextant.context.Properties.defaultContext
import hextant.context.Properties.projectContext
import hextant.context.createControl
import hextant.fx.initHextantScene
import hextant.plugins.initializePluginsFromClasspath
import hextant.project.ProjectType
import hextant.serial.makeRoot
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import kotlin.reflect.KClass

/**
 * Abstract base class for GUI testing applications.
 */
abstract class HextantTestApplication(private val projectType: ProjectType) : Application() {
    /**
     * The main [Stage] that displays the content.
     */
    private lateinit var stage: Stage

    /**
     * Starts the application.
     */
    final override fun start(primaryStage: Stage) {
        val cl = javaClass.classLoader
        val projectContext = projectContext(Context.newInstance(), cl)
        val context = defaultContext(projectContext)
        initializePluginsFromClasspath(context, testing = true)
        projectType.initializeContext(context)
        val editor = projectType.createProject(context)
        editor.makeRoot()
        val view = context.createControl(editor)
        val scene = Scene(view)
        scene.initHextantScene(context)
        primaryStage.scene = scene
        stage = primaryStage
    }

    companion object {
        /**
         * Launches the [HextantTestApplication] [A].
         */
        inline fun <reified A : HextantTestApplication> launch() {
            launch(A::class)
        }

        /**
         * Launches the [HextantTestApplication] with the given class.
         */
        fun launch(cls: KClass<out HextantTestApplication>) {
            launch(cls.java)
        }
    }
}