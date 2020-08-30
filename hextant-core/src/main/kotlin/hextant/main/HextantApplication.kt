/**
 *@author Nikolaus Knop
 */

package hextant.main

import bundles.Property
import hextant.context.Context
import hextant.context.Internal
import hextant.fx.initHextantScene
import hextant.main.HextantPlatform.defaultContext
import hextant.main.HextantPlatform.projectContext
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import kotlin.concurrent.thread
import kotlin.reflect.KClass

/**
 * Abstract base class for applications that use the Hextant framework.
 */
abstract class HextantApplication : Application() {
    /**
     * The main [Stage] that displays the content.
     */
    protected lateinit var stage: Stage
        private set

    /**
     * Starts the application.
     */
    final override fun start(primaryStage: Stage) {
        val preloader = HextantPreloader()
        preloader.start(Stage())
        val context = defaultContext(projectContext(Context.newInstance()))
        val scene = Scene(createView(context))
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

    /**
     * Initialize the context.
     *
     * The default implementation does nothing.
     */
    protected open fun Context.initializeContext() {}

    /**
     * Create the root node using the given [context].
     */
    protected abstract fun createView(context: Context): Parent

    companion object {
        /**
         * The main stage
         */
        val stage = Property<Stage, Internal, Internal>("stage")

        /**
         * Launches the [HextantApplication] [A].
         */
        inline fun <reified A : HextantApplication> launch() {
            launch(A::class)
        }

        /**
         * Launches the [HextantApplication] with the given class.
         */
        fun launch(cls: KClass<out HextantApplication>) {
            launch(cls.java)
        }
    }
}