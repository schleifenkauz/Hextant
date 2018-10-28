package org.nikok.hextant.core.mocks

import org.nikok.hextant.Editable
import org.nikok.hextant.EditorView
import org.nikok.hextant.core.base.AbstractEditor

internal class MockEditor(
    editable: Editable<Unit> = MockEditable()
) : AbstractEditor<Editable<Unit>, EditorView>(editable)