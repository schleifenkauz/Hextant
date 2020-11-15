package hextant.core

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import com.nhaarman.mockitokotlin2.verify
import hextant.context.SelectionDistributor
import hextant.test.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

internal object SelectionDistributorSpec : Spek({
    val sut = SelectionDistributor.newInstance()
    val ctx = testingContext()
    given("a selection distributor") {
        test("no editors should be selected") {
            sut.selectedTargets.now shouldMatch isEmpty
        }
        test("no views should be selected") {
            sut.selectedViews.now shouldMatch isEmpty
        }
        val view1 = mockView<EditorView>(mockEditor(ctx))
        on("selecting an editor when no other is selected") {
            val selected = sut.select(view1)
            it("should return true") {
                selected shouldBe `true`
            }
            it("should add selection to the view") {
                sut.selectedViews.now shouldBe equalTo(setOf(view1))
            }
            it("should add selection to the editor") {
                sut.selectedTargets.now shouldBe equalTo(setOf(view1.target))
            }
        }
        val view2 = mockView<EditorView>(mockEditor(ctx))
        on("selecting another editor") {
            val selected = sut.select(view2)
            it("should return true") {
                selected shouldBe `true`
            }
            it("should deselect the old editor and select the new one") {
                sut.selectedTargets.now shouldBe equalTo(setOf(view2.target))
            }
            it("should deselect the old view and select the new one") {
                sut.selectedViews.now shouldBe equalTo(setOf(view2))
            }
            it("should deselect the old editor") {
                verify(view1).deselect()
            }
        }
        on("toggling selection for the old triple") {
            val selected = sut.toggleSelection(view1)
            it("should return true") {
                selected shouldBe `true`
            }
            it("should select the new editor") {
                sut.selectedTargets.now shouldBe equalTo(setOf(view1.target, view2.target))
            }
            it("should select the new view") {
                sut.selectedViews.now shouldBe equalTo(setOf(view1, view2))
            }
        }
        on("toggling selection for the second triple") {
            val selected = sut.toggleSelection(view2)
            it("should return false") {
                selected shouldBe `false`
            }
            it("should deselect the second editor") {
                sut.selectedTargets.now shouldBe equalTo(setOf(view1.target))
            }
            it("should deselect the second view") {
                sut.selectedViews.now shouldBe equalTo(setOf(view1))
            }
            it("should call the second deselect") {
                verify(view2).deselect()
            }
        }
        on("selecting the first editor") {
            val selected = sut.select(view1)
            it("should return true") {
                selected shouldBe `true`
            }
            it("should do nothing else") {
                sut.selectedViews.now shouldBe equalTo(setOf(view1))
                sut.selectedTargets.now shouldBe equalTo(setOf(view1.target))
            }
        }
    }
})