package hextant.test

import hextant.context.*
import hextant.fx.initHextantScene
import hextant.main.HextantPlatform
import hextant.main.initializePluginsFromClasspath
import hextant.project.ProjectType
import hextant.serial.makeRoot
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage
import kotlin.concurrent.thread
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
        val preloader = HextantPreloader()
        preloader.start(Stage())
        val context = Properties.defaultContext(HextantPlatform.projectContext(Context.newInstance()))
        initializePluginsFromClasspath(context, testing = true)
        projectType.initializeContext(context)
        val editor = projectType.createProject(context)
        editor.makeRoot()
        val view = context.createControl(editor)
        val scene = Scene(view)
        scene.initHextantScene(context)
        primaryStage.scene = scene
        stage = primaryStage
        showPreloader(preloader, primaryStage)
    }

    private fun showPreloader(preloader: HextantPreloader, stage: Stage) {
        thread {
            for (i in 0..100) {
                Platform.runLater {
                    preloader.setProgress(i * 0.01)
                }
                Thread.sleep(10)
            }
            Platform.runLater {
                preloader.hide()
                stage.show()
            }
        }
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