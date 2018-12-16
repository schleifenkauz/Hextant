package org.nikok.hextant.core.mocks

import org.nikok.hextant.*
import org.nikok.hextant.core.base.AbstractEditor

internal class MockEditor(
    editable: Editable<Unit> = EditableMock(),
    platform: HextantPlatform
) : AbstractEditor<Editable<Unit>, EditorView>(editable, platform)