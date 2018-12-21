/**
 *@author Nikolaus Knop
 */

package hextant.core

import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.throws
import hextant.core.expr.editable.EditableIntLiteral
import hextant.core.expr.edited.IntLiteral
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

@Suppress("UNUSED_PARAMETER")
internal object EditableFactorySpec: Spek({
    describe("registering") {
        val ef = EditableFactory.newInstance()
        on("registering a editable for a class") {
            ef.register { il: IntLiteral -> EditableIntLiteral(il.value) }
            ef.register { -> EditableIntLiteral() }
            it("should return the registered editable when getting an Editable for the registered class") {
                ef.getEditable(IntLiteral::class) shouldMatch instanceOf<EditableIntLiteral>()
            }
            it("should return the registered editable when getting an Editable for the registered value") {
                ef.getEditable(IntLiteral(2)) shouldMatch instanceOf<EditableIntLiteral>()
            }
        }
        on("asking for a editable of a subtype of a registered type") {
            val anyEditor = ef.getEditable(Any::class)
            it("should return an editable of the nearest registered subclass") {
                anyEditor shouldMatch instanceOf<EditableIntLiteral>()
            }
        }
        on("getting an editable for an unregistered class") {
            val error = { ef.getEditable(Int::class); Unit }
            it("should throw a NoSuchElementException") {
                error shouldMatch throws<NoSuchElementException>()
            }
        }
    }
})