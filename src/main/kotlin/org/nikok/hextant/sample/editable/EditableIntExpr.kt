/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.sample.editable

import org.nikok.hextant.sample.ast.IntExpr
import org.nikok.reaktive.value.ReactiveValue

interface EditableIntExpr {
    val expr: ReactiveValue<IntExpr?>
}