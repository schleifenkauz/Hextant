package org.nikok.hextant.core.mocks

import org.nikok.hextant.*
import org.nikok.hextant.core.base.AbstractEditor

internal class MockEditor(
    editable: Editable<Unit> = EditableMock(),
    context: Context
) : AbstractEditor<Editable<Unit>, EditorView>(editable, context)