/**
 *@author Nikolaus Knop
 */

package hextant.core

import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.throws
import hextant.EditorFactory
import hextant.expr.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.register
import hextant.test.*
import org.jetbrains.spek.api.Spek

@Suppress("UNUSED_PARAMETER")
internal object EditorFactorySpec : Spek({
    DESCRIBE("registering") {
        val ef = EditorFactory.newInstance()
        val context = testingContext()
        ON("registering a editable for a class") {
            ef.register { context, il: IntLiteral -> IntLiteralEditor(il, context) }
            ef.register { context -> IntLiteralEditor(context) }
            IT("should return the registered editable when getting an editable for the registered class") {
                ef.getEditor(IntLiteral::class, context) shouldMatch instanceOf<IntLiteralEditor>()
            }
            IT("should return the registered editable when getting an editable for the registered value") {
                ef.getEditor(IntLiteral(2), context) shouldMatch instanceOf<IntLiteralEditor>()
            }
        }
        ON("asking for a editable of a subtype of a registered type") {
            val anyEditor = ef.getEditor(Any::class, context)
            IT("should return an editable of the nearest registered subclass") {
                anyEditor shouldMatch instanceOf<IntLiteralEditor>()
            }
        }
        ON("getting an editable for an unregistered class") {
            val error = { ef.getEditor(Int::class, context); Unit }
            IT("should throw a nosuchelementexception") {
                error shouldMatch throws<NoSuchElementException>()
            }
        }
    }
})