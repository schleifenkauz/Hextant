///**
// *@author Nikolaus Knop
// */
//
//package hextant.core.editor
//
//import hextant.*
//import hextant.base.*
//import hextant.core.mocks.EditableMock
//import hextant.test.matchers.*
//import org.jetbrains.spek.api.Spek
//import org.jetbrains.spek.api.dsl.given
//import org.jetbrains.spek.api.dsl.on
//import reaktive.value.reactiveValue
//
//internal object ExtendShrinkSelectionSpec : Spek({
//    class Parent : AbstractEditable<Unit>() {
//        override val result: RResult<Unit>
//            get() = reactiveValue(Ok(Unit))
//    }
//
//    class ChildEditor(context: Context) :
//        AbstractEditor<EditableMock, EditorView, Any?>(EditableMock(), context)
//
//    class AParentEditor(context: Context) : ParentEditor<Parent, EditorView>(Parent(), context) {
//        override fun accepts(child: Editor<*>): Boolean = child is ChildEditor
//    }
//
//    given("3 children and a parent") {
//        val platform = HextantPlatform.configured()
//        val children = List(3) { ChildEditor(platform) }
//        val parent = AParentEditor(platform)
//        for (it in children) {
//            it.moveTo(parent)
//        }
//        val second = children[1]
//        val third = children[2]
//        on("selecting the second and the third child and the extending selection") {
//            second.select()
//            third.toggleSelection()
//            parent.extendSelection(second)
//            test("the second child should not be selected") {
//                second.isSelected shouldBe `false`
//            }
//            test("the third child should still be selected") {
//                third.isSelected shouldBe `true`
//            }
//            test("the parent should be selected") {
//                parent.isSelected shouldBe `true`
//            }
//        }
//        on("shrinking selection on the parent") {
//            parent.shrinkSelection()
//            test("the second and the third child should be selected") {
//                second.isSelected shouldBe `true`
//                third.isSelected shouldBe `true`
//            }
//            test("the parent should not be selected") {
//                parent.isSelected shouldBe `false`
//            }
//        }
//    }
//})