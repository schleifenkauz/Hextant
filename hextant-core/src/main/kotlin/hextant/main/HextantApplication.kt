/**
 *@author Nikolaus Knop
 */

package hextant.main

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.text.Font
import javafx.stage.Stage
import kotlin.concurrent.thread

class HextantApplication : Application() {
    override fun start(stage: Stage) {
        val preloader = HextantPreloader()
        preloader.start(Stage())
        stage.scene = Scene(
            Label("This is Hextant").apply {
                font = Font(20.0)
            }
        )
        thread {
            for (i in 0..100) {
                Platform.runLater {
                    preloader.setProgress(i * 0.01)
                }
                Thread.sleep(100)
            }
            Platform.runLater {
                preloader.hide()
                stage.show()
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(HextantApplication::class.java)
        }
    }
}