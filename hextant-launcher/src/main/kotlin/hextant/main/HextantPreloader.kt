/**
 *@author Nikolaus Knop
 */

package hextant.main

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlin.concurrent.thread

internal class HextantPreloader : Application() {
    private lateinit var progressBar: ProgressBar
    private lateinit var info: Label
    private lateinit var stage: Stage

    override fun start(primaryStage: Stage) {
        stage = primaryStage
        val image = javaClass.getResource(PRELOADER_IMAGE).toExternalForm()
        val view = ImageView(image)
        progressBar = ProgressBar()
        val root = VBox(view, progressBar, info)
        progressBar.prefWidth = view.prefWidth(-1.0)
        stage.scene = Scene(root)
        stage.show()
    }

    inline fun showWhileExecuting(action: () -> Unit) {
        val st = Stage()
        start(st)
        thread {
            for (i in 0..100) {
                progressBar.progress = i * 0.01
            }
        }
        action()
        st.hide()
    }

    companion object {
        private const val PRELOADER_IMAGE = "preloader-image.jpg"
    }
}