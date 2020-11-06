package hextant.fx

import bundles.SimpleProperty

/**
 * Used to configure the size of the window that is used to display a hextant project.
 */
sealed class WindowSize {
    /**
     * Indicates that the window should be maximized.
     */
    object Maximized : WindowSize()

    /**
     * Indicates that the window should be shown in full screen.
     */
    object FullScreen : WindowSize()

    /**
     * Indicates that the size of the window should be set to the size of the scene's root.
     */
    object FitContent : WindowSize()

    /**
     * Indicates that the windows size should not be modified.
     */
    object Default : WindowSize()

    /**
     * Indicates that the windows size should be set to the given [width] and [height].
     * @property width the width of the window
     * @property height the height of the window
     */
    data class Configured(val width: Double, val height: Double) : WindowSize()

    companion object : SimpleProperty<WindowSize>("window size") {
        override val default: WindowSize = Default
    }
}