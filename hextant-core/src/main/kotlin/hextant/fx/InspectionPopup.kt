/**
 *@author Nikolaus Knop
 */

package hextant.fx

import hextant.context.*
import hextant.inspect.*
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.input.KeyCode.ENTER
import javafx.scene.input.KeyCodeCombination
import javafx.scene.layout.VBox
import javafx.stage.Popup

/**
 * A [Popup] that displays the problems of the assigned target.
 */
class InspectionPopup(private val context: Context, private val target: Any) : Popup() {
    private fun problems() = context[Inspections].getProblems(target).sortedBy { it.severity }

    private val container = VBox()

    init {
        context[Internal, Stylesheets].apply(scene)
        container.styleClass.add("problem-list")
        setOnShowing { update() }
        isAutoHide = true
        scene.registerShortcuts {
            on("ESCAPE") {
                hide()
            }
        }
        scene.root = container
    }

    private fun update() {
        container.children.clear()
        for (p in problems()) {
            container.children.add(problemLabel(p))
        }
    }

    private fun problemLabel(problem: Problem<Any>): Node = Button().apply {
        styleClass.add("problem")
        styleClass.add("${problem.severity}-item")
        text = problem.message
        isFocusTraversable = true
        val fixes = problem.fixes
        if (fixes.isNotEmpty()) {
            val fixesPopup = FixesPopup(target, context, fixes)
            setOnAction { showFixes(fixesPopup) }
            registerShortcut(KeyCodeCombination(ENTER)) { showFixes(fixesPopup) }
        }
    }

    private fun Button.showFixes(fixesPopup: FixesPopup) {
        val owner = scene.root
        val anchor = owner.localToScreen(boundsInLocal)
        fixesPopup.show(owner, anchor.maxX, anchor.minY)
    }

    @Suppress("KDocMissingDocumentation")
    override fun show() {
        if (problems().isNotEmpty()) {
            super.show()
        }
        if (isShowing) {
            val firstProblem = container.children.first()
            firstProblem.requestFocus()
        }
    }

    private class FixesPopup(
        private val target: Any,
        private val context: Context,
        private val fixes: Collection<ProblemFix<Any>>
    ) : Popup() {
        init {
            context[Internal, Stylesheets].apply(scene)
            content.add(createFixList(fixes))
        }

        override fun show() {
            if (fixes.isNotEmpty()) super.show()
        }

        private fun createFixList(fixes: Collection<ProblemFix<Any>>): Parent {
            val container = VBox()
            container.styleClass.add("fix-list")
            for (f in fixes) {
                container.children.add(fixLabel(f))
            }
            return container
        }

        private fun fixLabel(fix: ProblemFix<Any>): Node = Button().apply {
            styleClass.add("problem-fix")
            text = fix.description
            isFocusTraversable = true
            registerShortcut(KeyCodeCombination(ENTER)) { applyAndHide(fix) }
            setOnMouseClicked { applyAndHide(fix) }
        }

        private fun applyAndHide(fix: ProblemFix<Any>) {
            fix.run {
                context.executeSafely("Executing ${fix.description}", Unit) {
                    InspectionBody.of(target).fix()
                }
            }
            hide()
            ownerWindow.hide()
        }
    }
}