package org.nikok.hextant.sample.editor

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.on
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.sample.ast.IntLiteral
import org.nikok.hextant.sample.editable.EditableIntLiteral
import org.nikok.reaktive.value.now

internal object IntLiteralEditorSpec : Spek({
    given("an int literal editor") {
        val platform = HextantPlatform.newInstance()
        val editable = EditableIntLiteral()
        val editor = IntLiteralEditor(editable, platform)
        test("the int literal should initially be null") {
            editable.edited.now shouldMatch absent()
        }
        test("the editable should not be ok") {
            editable.isOk.now shouldMatch equalTo(false)
        }
        on("setting the text to a valid integer literal") {
            editor.setText("124")
            test("the editable should be ok") {
                editable.isOk.now shouldMatch equalTo(true)
            }
            test("the int literal should be parsed") {
                editable.edited.now shouldMatch equalTo(IntLiteral(124))
            }
        }
        on("setting the text to a invalid integer literal") {
            editor.setText("invalid")
            test("the integer literal should not be ok") {
                editable.isOk.now shouldMatch equalTo(false)
            }
            test("the integer literal should equal null") {
                editable.edited.now shouldMatch absent()
            }
        }
    }
})
