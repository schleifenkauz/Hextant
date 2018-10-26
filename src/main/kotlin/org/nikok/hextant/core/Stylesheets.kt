package org.nikok.hextant.core

import javafx.scene.Scene

object Stylesheets {
    private val cssFiles = Resources.allCSS()

    private val stringified by lazy { cssFiles.map { it.toUri().toURL().toString() } }

    fun apply(stylesheets: MutableCollection<String>) {
        stylesheets.addAll(stringified)
    }

    fun apply(scene: Scene) {
        apply(scene.stylesheets)
    }
}