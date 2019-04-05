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
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.text.Font
import javafx.stage.Stage
import kotlin.concurrent.thread

abstract class HextantApplication : Application() {
    final override fun start(stage: Stage) {
        val preloader = HextantPreloader()
        preloader.start(Stage())
        stage.scene = hextantScene(this::createView, this::createContext)
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

    protected abstract fun createContext(platform: HextantPlatform): Context

    protected abstract fun createView(context: Context): Parent
}