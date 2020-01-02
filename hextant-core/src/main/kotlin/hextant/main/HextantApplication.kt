/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.Context
import hextant.HextantPlatform
import hextant.fx.hextantScene
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Parent
import javafx.stage.Stage
import kotlin.concurrent.thread

abstract class HextantApplication : Application() {
    protected lateinit var stage: Stage
        private set

    final override fun start(primaryStage: Stage) {
        val preloader = HextantPreloader()
        preloader.start(Stage())
        primaryStage.scene = hextantScene(this::createView, this::createContext)
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
                stage.isFullScreen = true
                stage.show()
            }
        }
    }

    protected abstract fun createContext(platform: HextantPlatform): Context

    protected abstract fun createView(context: Context): Parent
}