/**
 *@author Nikolaus Knop
 */

package hextant.core

import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.throws
import hextant.context.*
import hextant.expr.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.test.instanceOf
import hextant.test.testingContext
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

@Suppress("UNUSED_PARAMETER")
internal object EditorFactorySpec : Spek({
    describe("registering") {
        val ef = EditorFactory.newInstance()
        val context = testingContext()
        on("registering a editable for a class") {
            ef.register { context, il: IntLiteral -> IntLiteralEditor(il, context) }
            ef.register { context -> IntLiteralEditor(context) }
            it("should return the registered editable when getting an editable for the registered class") {
                ef.createEditor<IntLiteral>(context) shouldMatch instanceOf<IntLiteralEditor>()
            }
            it("should return the registered editable when getting an editable for the registered value") {
                ef.createEditor(IntLiteral(2), context) shouldMatch instanceOf<IntLiteralEditor>()
            }
        }
        on("asking for a editable of a subtype of a registered type") {
            val anyEditor = ef.createEditor<Any>(context)
            it("should return an editable of the nearest registered subclass") {
                anyEditor shouldMatch instanceOf<IntLiteralEditor>()
            }
        }
        on("getting an editable for an unregistered class") {
            val error = { ef.createEditor<Int>(context); Unit }
            it("should throw a NoSuchElementException") {
                error shouldMatch throws<NoSuchElementException>()
            }
        }
    }
})