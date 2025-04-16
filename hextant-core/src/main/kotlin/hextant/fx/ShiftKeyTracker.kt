package hextant.fx

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import java.util.logging.Level
import java.util.logging.Logger

internal object ShiftKeyTracker : NativeKeyListener {
    var isShiftDown: Boolean = false
        private set

    private var isStarted = false

    fun start() {
        if (isStarted) return
        val logger = Logger.getLogger(GlobalScreen::class.java.packageName)
        logger.level = Level.OFF
        logger.useParentHandlers = false

        try {
            GlobalScreen.registerNativeHook()
        } catch (ex: NativeHookException) {
            println("Failed to register native hook:")
            ex.printStackTrace()
            return
        }
        GlobalScreen.addNativeKeyListener(this)
        isStarted = true
    }

    override fun nativeKeyPressed(e: NativeKeyEvent) {
        if (e.keyCode == NativeKeyEvent.VC_SHIFT) {
            isShiftDown = true
        }
    }

    override fun nativeKeyReleased(e: NativeKeyEvent) {
        if (e.keyCode == NativeKeyEvent.VC_SHIFT) {
            isShiftDown = false
        }
    }
}