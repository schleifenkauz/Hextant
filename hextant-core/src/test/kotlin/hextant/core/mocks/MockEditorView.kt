package hextant.core.mocks

import hextant.EditorView
import hextant.ViewGroup
import hextant.bundle.Bundle

internal class MockEditorView: EditorView {
    override val arguments: Bundle = Bundle.newInstance()

    override val target: Any
        get() = Any()

    override fun deselect() {

    }

    override fun focus() {
        TODO("not implemented")
    }

    override val group: ViewGroup<*>
        get() = TODO("not implemented")
}