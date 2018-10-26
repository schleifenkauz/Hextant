package org.nikok.hextant.core.mocks

import org.nikok.hextant.*
import org.nikok.hextant.core.base.AbstractEditor

internal class MockEditor(
    editable: Editable<Unit> = MockEditable(),
    view: EditorView = MockEditorView()
) : AbstractEditor<Editable<Unit>>(editable, view)