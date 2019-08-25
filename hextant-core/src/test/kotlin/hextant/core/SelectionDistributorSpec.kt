package hextant.core

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import com.nhaarman.mockitokotlin2.verify
import hextant.EditorView
import hextant.impl.SelectionDistributor
import hextant.test.*
import org.jetbrains.spek.api.Spek

internal object SelectionDistributorSpec: Spek({
    val sut = SelectionDistributor.newInstance()
    GIVEN("a selection distributor") {
        test("no editors should be selected") {
            sut.selectedTargets.now shouldMatch isEmpty
        }
        test("no views should be selected") {
            sut.selectedViews.now shouldMatch isEmpty
        }
        val view1 = mockView<EditorView>()
        ON("selecting an editor when no other is selected") {
            val selected = sut.select(view1)
            IT("should return true") {
                selected shouldBe `true`
            }
            IT("should add selection to the view") {
                sut.selectedViews.now shouldBe equalTo(setOf(view1))
            }
            IT("should add selection to the editor") {
                sut.selectedTargets.now shouldBe equalTo(setOf(view1.target))
            }
        }
        val view2 = mockView<EditorView>()
        ON("selecting another editor") {
            val selected = sut.select(view2)
            IT("should return true") {
                selected shouldBe `true`
            }
            IT("should deselect the old editor and select the new one") {
                sut.selectedTargets.now shouldBe equalTo(setOf(view2.target))
            }
            IT("should deselect the old view and select the new one") {
                sut.selectedViews.now shouldBe equalTo(setOf(view2))
            }
            IT("should deselect the old editor") {
                verify(view1).deselect()
            }
        }
        ON("toggling selection for the old triple") {
            val selected = sut.toggleSelection(view1)
            IT("should return true") {
                selected shouldBe `true`
            }
            IT("should select the new editor") {
                sut.selectedTargets.now shouldBe equalTo(setOf(view1.target, view2.target))
            }
            IT("should select the new view") {
                sut.selectedViews.now shouldBe equalTo(setOf(view1, view2))
            }
        }
        ON("toggling selection for the second triple") {
            val selected = sut.toggleSelection(view2)
            IT("should return false") {
                selected shouldBe `false`
            }
            IT("should deselect the second editor") {
                sut.selectedTargets.now shouldBe equalTo(setOf(view1.target))
            }
            IT("should deselect the second view") {
                sut.selectedViews.now shouldBe equalTo(setOf(view1))
            }
            IT("should call the second deselect") {
                verify(view2).deselect()
            }
        }
        ON("selecting the first editor") {
            val selected = sut.select(view1)
            IT("should return true") {
                selected shouldBe `true`
            }
            IT("should do nothing else") {
                sut.selectedViews.now shouldBe equalTo(setOf(view1))
                sut.selectedTargets.now shouldBe equalTo(setOf(view1.target))
            }
        }
    }
})