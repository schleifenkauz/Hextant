package hextant.core.mocks

import hextant.EditorView
import hextant.ViewGroup
import hextant.bundle.Bundle
import hextant.bundle.createBundle

internal class MockEditorView : EditorView {
    override val arguments: Bundle = createBundle()

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