/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core

import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.throws
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.edited.IntLiteral

internal typealias EditableInEditablePackage = org.nikok.hextant.core.mocks.editable.EditableEdited
internal typealias EditableInSamePackage = org.nikok.hextant.core.mocks.EditableEdited

@Suppress("UNUSED_PARAMETER")
internal object EditableFactorySpec: Spek({
    describe("registering") {
        val ef = EditableFactory.newInstance(EditableFactorySpec.javaClass.classLoader)
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
    group("conventions") {
        describe("editable in same package") {
            val clsLoader = mock<ClassLoader> {
                on { loadClass(EditableInSamePackage::class.qualifiedName) } doReturn EditableInSamePackage::class.java
            }
            val ef = EditableFactory.newInstance(clsLoader)
            on("getting an editable for a class") {
                val cls = org.nikok.hextant.core.mocks.Edited::class
                val editable = ef.getEditable(cls)
                it("should find the Editable in the same package and call the right constructor") {
                    editable shouldMatch instanceOf<EditableInSamePackage>()
                }
            }
            on("getting an editable for an instance") {
                val edited = org.nikok.hextant.core.mocks.Edited
                val editable = ef.getEditable(edited)
                it("should find the Editable in the same package and call the right constructor") {
                    editable shouldMatch instanceOf<EditableInSamePackage>()
                }
            }
        }
        describe("Editable in child package named 'editable'") {
            val clsLoader = mock<ClassLoader> {
                on { loadClass(EditableInEditablePackage::class.qualifiedName) } doReturn EditableInEditablePackage::class.java
            }
            val ef = EditableFactory.newInstance(clsLoader)
            on("getting an editable for a class") {
                val cls = org.nikok.hextant.core.mocks.Edited::class
                val editable = ef.getEditable(cls)
                it("should find the Editable in the 'editable' package and call the right constructor") {
                    editable shouldMatch instanceOf<EditableInEditablePackage>()
                }
            }
            on("getting an editable for an edited that has a Editable in a child package named ''") {
                val edited = org.nikok.hextant.core.mocks.Edited
                val editable = ef.getEditable(edited)
                it("should find the Editable in the 'editable' package and call the right constructor") {
                    editable shouldMatch instanceOf<EditableInEditablePackage>()
                }
            }
        }
        describe("Editable in sibling package named 'editable'") {
            val clsLoader = mock<ClassLoader> {
                on { loadClass(EditableInEditablePackage::class.qualifiedName) } doReturn EditableInEditablePackage::class.java
            }
            val ef = EditableFactory.newInstance(clsLoader)
            on("getting an editable for a class") {
                val cls = org.nikok.hextant.core.mocks.edited.Edited::class
                val editable = ef.getEditable(cls)
                it("should find the Editable in the 'editable' package and call the right constructor") {
                    editable shouldMatch instanceOf<EditableInEditablePackage>()
                }
            }
            on("getting an editable for an edited that has a Editable in a child package named ''") {
                val edited = org.nikok.hextant.core.mocks.edited.Edited
                val editable = ef.getEditable(edited)
                it("should find the Editable in the 'editable' package and call the right constructor") {
                    editable shouldMatch instanceOf<EditableInEditablePackage>()
                }
            }
        }
    }
})