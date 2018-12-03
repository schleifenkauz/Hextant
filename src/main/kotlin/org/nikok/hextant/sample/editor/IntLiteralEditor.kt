/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editor

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.DefaultRegistration
import org.nikok.hextant.core.editable.EditableToken
import org.nikok.hextant.core.editor.TokenEditor
import org.nikok.hextant.sample.ast.IntLiteral

class IntLiteralEditor(
    editable: EditableToken<IntLiteral>,
    platform: HextantPlatform
) : TokenEditor<IntLiteral>(editable, platform) {
    init {
        registerDefault(platform)
    }

    companion object : DefaultRegistration({

    })
}