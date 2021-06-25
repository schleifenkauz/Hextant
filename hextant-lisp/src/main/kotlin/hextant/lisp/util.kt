package hextant.lisp

import hextant.fx.showDialog
import hextant.lisp.rt.LispRuntimeException
import javafx.scene.control.Alert

fun tryWithExceptionAlert(action: () -> Unit) {
    try {
        action()
    } catch (ex: LispRuntimeException) {
        Alert(Alert.AlertType.ERROR, ex.message).showDialog()
        ex.printStackTrace()
    }
}