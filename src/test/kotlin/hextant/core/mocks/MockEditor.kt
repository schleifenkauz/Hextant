package hextant.core.mocks

import hextant.*
import hextant.base.AbstractEditor

internal class MockEditor(
    editable: Editable<Unit> = EditableMock(),
    context: Context
) : AbstractEditor<Editable<Unit>, EditorView>(editable, context)