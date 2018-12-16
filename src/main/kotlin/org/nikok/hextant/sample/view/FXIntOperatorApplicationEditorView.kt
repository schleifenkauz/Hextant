/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.view

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.CompoundEditorControl
import org.nikok.hextant.sample.editable.EditableIntOperatorApplication

class FXIntOperatorApplicationEditorView(editable: EditableIntOperatorApplication, platform: HextantPlatform) :
    CompoundEditorControl(platform, {
        line {
            view(editable.left)
            view(editable.op)
            view(editable.right)
        }
    })