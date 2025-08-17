package hextant.fx

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import reaktive.value.ReactiveBoolean
import reaktive.value.now
import reaktive.value.reactiveVariable
import java.util.logging.Level
import java.util.logging.Logger

object ModifierKeyTracker {
    private val shiftDown = reactiveVariable(false)
    private val altDown = reactiveVariable(false)
    private val ctrlDown = reactiveVariable(false)

    val isShiftDown: ReactiveBoolean get() = shiftDown
    val isAltDown: ReactiveBoolean get() = altDown
    val isCtrlDown: ReactiveBoolean get() = ctrlDown

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
        GlobalScreen.addNativeKeyListener(Listener)
        isStarted = true
    }

    private object Listener: NativeKeyListener {
        override fun nativeKeyPressed(e: NativeKeyEvent) {
            when (e.keyCode) {
                NativeKeyEvent.VC_SHIFT -> shiftDown.now = true
                NativeKeyEvent.VC_ALT -> altDown.now = true
                NativeKeyEvent.VC_CONTROL -> ctrlDown.now = true
            }
        }

        override fun nativeKeyReleased(e: NativeKeyEvent) {
            when (e.keyCode) {
                NativeKeyEvent.VC_SHIFT -> shiftDown.now = false
                NativeKeyEvent.VC_ALT -> altDown.now = false
                NativeKeyEvent.VC_CONTROL -> ctrlDown.now = false
            }
        }
    }
}