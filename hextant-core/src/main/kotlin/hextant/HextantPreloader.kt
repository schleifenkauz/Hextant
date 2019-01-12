/**
 *@author Nikolaus Knop
 */

package hextant

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.ProgressBar
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.stage.Stage

class HextantPreloader : Application() {
    private lateinit var progressBar: ProgressBar
    private lateinit var stage: Stage

    override fun start(primaryStage: Stage) {
        stage = primaryStage
        val hextantImageURL = javaClass.getResource(PRELOADER_IMAGE).toExternalForm()
        val image = Image(hextantImageURL)
        val view = ImageView(image)
        progressBar = ProgressBar()
        val root = VBox(view, progressBar)
        progressBar.prefWidth = view.prefWidth(-1.0)
        stage.scene = Scene(root)
        stage.show()
    }

    fun setProgress(d: Double) {
        progressBar.progress = d
    }

    fun hide() {
        stage.hide()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(HextantPreloader::class.java)
        }

        private const val PRELOADER_IMAGE = "preloader-image.jpg"
    }
}