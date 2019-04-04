package hextant.base

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.should.shouldNotMatch
import com.sun.javafx.application.PlatformImpl
import hextant.HextantPlatform
import hextant.bundle.Bundle
import hextant.core.instanceOf
import hextant.core.mocks.EditableMock
import hextant.core.mocks.MockEditor
import hextant.fx.PseudoClasses.ERROR
import hextant.fx.PseudoClasses.SELECTED
import javafx.scene.Scene
import javafx.scene.control.Label
import matchers.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

internal object EditorControlSpec : Spek({
    PlatformImpl.startup { }
    given("a editor control") {
        val ec = AnEditorControl()
        Scene(ec) //set the scene of the control
        context("roots") {
            test("the root should be the default root") {
                ec.root.text shouldBe equalTo("default")
            }
            test("the skin should be a simple skin with the default root") {
                ec.skin.node shouldBe instanceOf<Label>()
            }
            on("setting the root") {
                ec.setRoot(Label("custom"))
                it("should update the root") {
                    ec.root.text shouldBe equalTo("custom")
                }
                it("should update the skin") {
                    (ec.skin.node as Label).text shouldBe equalTo("custom")
                }
            }
        }
        context("view actions") {
            on("select") {
                ec.select(true)
                it("should have the selected pseudo class") {
                    ec.pseudoClassStates shouldMatch hasElement(SELECTED)
                }
            }
            on("deselect") {
                ec.select(false)
                it("shouldn't have the selected pseudo class") {
                    ec.root.pseudoClassStates shouldNotMatch hasElement(SELECTED)
                }
            }
            on("error") {
                ec.error(true)
                it("should have the error pseudo class") {
                    ec.root.pseudoClassStates shouldMatch hasElement(ERROR)
                }
            }
            on("no error") {
                ec.error(false)
                it("should not have the error pseudo class") {
                    ec.root.pseudoClassStates shouldNotMatch hasElement(ERROR)
                }
            }
        }
        afterGroup { PlatformImpl.exit() }
    }
}) {
    class AnEditorControl : EditorControl<Label>(Bundle.newInstance()) {
        override fun createDefaultRoot(): Label = Label("default")

        fun setRoot(new: Label) {
            root = new
        }

        override fun receiveFocus() {
            root.requestFocus()
        }

        init {
            val platform = HextantPlatform.configured()
            val editable = EditableMock()
            initialize(editable, MockEditor(editable, platform), platform)
        }
    }
}

