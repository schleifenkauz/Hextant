/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.Context
import hextant.HextantPlatform
import hextant.bundle.Internal
import hextant.bundle.Property
import hextant.fx.initHextantScene
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import kotlin.concurrent.thread
import kotlin.reflect.KClass

abstract class HextantApplication : Application() {
    protected lateinit var stage: Stage
        private set

    final override fun start(primaryStage: Stage) {
        val preloader = HextantPreloader()
        preloader.start(Stage())
        val rootCtx = HextantPlatform.rootContext()
        val context = createContext(rootCtx)
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

    protected open fun createContext(root: Context): Context = HextantPlatform.defaultContext(root)

    protected abstract fun createView(context: Context): Parent

    companion object {
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