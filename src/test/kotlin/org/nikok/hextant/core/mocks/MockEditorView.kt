package org.nikok.hextant.core.mocks

import org.nikok.hextant.EditorView

internal class MockEditorView: EditorView {
    override fun onGuiThread(action: () -> Unit) {
        action()
    }

    override fun focus() {}

    override fun select(isSelected: Boolean) {}

    override fun error(isError: Boolean) {}
}