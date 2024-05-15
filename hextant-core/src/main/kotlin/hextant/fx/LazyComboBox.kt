package hextant.fx

import javafx.scene.control.ComboBox

class LazyComboBox<E>(computeItems: () -> List<E>) : ComboBox<E>() {
    init {
        recomputeItemsBeforeShowingPopup(computeItems)
    }

    private fun recomputeItemsBeforeShowingPopup(computeItems: () -> List<E>) {
        setOnShowing {
            items.setAll(computeItems())
        }
    }
}