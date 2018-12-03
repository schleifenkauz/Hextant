package org.nikok.hextant.core.impl

import javafx.scene.Scene

internal object Stylesheets {
    private val cssFiles by lazy {
        Resources.allCSS()
    }

    private val stringified by lazy { cssFiles.map { it.toUri().toURL().toExternalForm() } }

    fun apply(stylesheets: MutableCollection<String>) {
        stylesheets.addAll(stringified)
    }

    fun apply(scene: Scene) {
        apply(scene.stylesheets)
    }
}