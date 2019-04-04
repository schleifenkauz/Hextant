package hextant.core.mocks

import hextant.EditorView
import hextant.bundle.Bundle

internal class MockEditorView: EditorView {
    override fun receiveFocus() {}

    override val arguments: Bundle = Bundle.newInstance()

    override fun onGuiThread(action: () -> Unit) {
        action()
    }

    override fun focus() {}

    override fun select(isSelected: Boolean) {}

    override fun error(isError: Boolean) {}

    override fun warn(warn: Boolean) {}
}