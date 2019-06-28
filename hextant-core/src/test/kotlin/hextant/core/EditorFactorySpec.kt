/**
 *@author Nikolaus Knop
 */

package hextant.core

import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.throws
import hextant.EditorFactory
import hextant.expr.edited.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.register
import hextant.test.matchers.instanceOf
import hextant.test.matchers.testingContext
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
            it("should return the registered editable when getting an Editable for the registered class") {
                ef.getEditor(IntLiteral::class, context) shouldMatch instanceOf<IntLiteralEditor>()
            }
            it("should return the registered editable when getting an Editable for the registered value") {
                ef.getEditor(IntLiteral(2), context) shouldMatch instanceOf<IntLiteralEditor>()
            }
        }
        on("asking for a editable of a subtype of a registered type") {
            val anyEditor = ef.getEditor(Any::class, context)
            it("should return an editable of the nearest registered subclass") {
                anyEditor shouldMatch instanceOf<IntLiteralEditor>()
            }
        }
        on("getting an editable for an unregistered class") {
            val error = { ef.getEditor(Int::class, context); Unit }
            it("should throw a NoSuchElementException") {
                error shouldMatch throws<NoSuchElementException>()
            }
        }
    }
})