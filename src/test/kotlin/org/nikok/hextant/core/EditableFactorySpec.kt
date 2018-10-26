/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core

import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.throws
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import org.nikok.hextant.Editable
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.edited.IntLiteral
import java.math.BigDecimal

internal object EditableFactorySpec: Spek({
    given("an editable factory") {
        val ef = EditableFactory.newInstance()
        on("registering a editable for a class") {
            ef.register(IntLiteral::class) { il: IntLiteral -> EditableIntLiteral(il.value) }
            ef.register(IntLiteral::class) { -> EditableIntLiteral() }
            it("should return the registered editable when getting an Editable for the registered class") {
                ef.getEditable(IntLiteral::class) shouldMatch instanceOf<EditableIntLiteral>()
            }
            it("should return the registered editable when getting an Editable for the registered value") {
                ef.getEditable(IntLiteral(2)) shouldMatch instanceOf<EditableIntLiteral>()
            }
        }
        on("registering a conversion for a source and a target") {
            ef.registerConversion(IntLiteral::class, Int::class) { it?.value }
            it("should convert when asking for a editable of the target class") {
                ef.getEditable(Int::class) shouldMatch instanceOf<Editable<Int>>()
            }
            xit("should convert when asking for a editable of a value of the target class") {
                ef.getEditable(1) shouldMatch instanceOf<Editable<Int>>()
            }
            it("should not override an already registered binding") {
                ef.registerConversion(Int::class, IntLiteral::class) { it?.let(::IntLiteral) }
                ef.getEditable(IntLiteral::class) shouldMatch instanceOf<EditableIntLiteral>()
            }
        }
        on("registering conversions that would lead to a cycle") {
            ef.registerConversion(Float::class, Double::class) { it?.toDouble() }
            ef.registerConversion(BigDecimal::class, Float::class) { it?.toFloat() }
            ef.registerConversion(Double::class, BigDecimal::class) { it?.toBigDecimal() }
            it("should not cycle but throw an exception") {
                val error = { ef.getEditable(Float::class); Unit }
                error shouldMatch throws<NoSuchElementException>()
            }
        }
        on("asking for a editable of a subtype of a registered type") {
            val anyEditor = ef.getEditable(Any::class)
            it("should return an editable of the nearest registered subclass") {
                anyEditor shouldMatch instanceOf<EditableIntLiteral>()
            }
        }
    }
})