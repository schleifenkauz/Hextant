package hextant.core.mocks

import hextant.EditorView
import hextant.bundle.Bundle

internal class MockEditorView: EditorView {
    override val arguments: Bundle = Bundle.newInstance()

    override val target: Any
        get() = Any()

    override fun deselect() {

    }
}