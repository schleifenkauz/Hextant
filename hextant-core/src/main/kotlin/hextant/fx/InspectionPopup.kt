/**
 *@author Nikolaus Knop
 */

package hextant.fx

import hextant.context.Context
import hextant.context.executeSafely
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
internal class InspectionPopup(private val context: Context, private val target: Any) : HextantPopup(context) {
    private fun problems() = context.executeSafely("getting problems", emptyList()) {
        context[Inspections].getProblems(target).sortedBy { it.severity }
    }

    private val container = VBox()

    init {
        container.styleClass.add("problem-list")
        scene.root = container
    }

    private fun update() {
        container.children.clear()
        for (p in problems()) {
            container.children.add(problemLabel(p))
        }
    }

    private fun <T : Any> problemLabel(problem: Problem<T>): Node = Button().apply {
        styleClass.add("problem")
        styleClass.add("${problem.severity}-item")
        text = problem.message
        isFocusTraversable = true
        val fixes = problem.fixes
        if (fixes.isNotEmpty()) {
            val fixesPopup = FixesPopup(problem.source, context, problem.fixes)
            setOnAction { showFixes(fixesPopup) }
            registerShortcut(KeyCodeCombination(ENTER)) { showFixes(fixesPopup) }
        }
    }

    private fun Button.showFixes(fixesPopup: FixesPopup<*>) {
        val owner = scene.root
        val anchor = owner.localToScreen(boundsInLocal)
        fixesPopup.show(owner, anchor.maxX, anchor.minY)
    }

    override fun show() {
        update()
        if (container.children.isNotEmpty()) {
            super.show()
        }
        if (isShowing) {
            val firstProblem = container.children.first()
            firstProblem.requestFocus()
        }
    }

    private class FixesPopup<T : Any>(
        private val target: InspectionBody<T>,
        private val context: Context,
        private val fixes: Collection<ProblemFix<T>>
    ) : HextantPopup(context) {
        init {
            context[Stylesheets].manage(scene)
            content.add(createFixList(fixes))
        }

        override fun show() {
            if (fixes.isNotEmpty()) super.show()
        }

        private fun createFixList(fixes: Collection<ProblemFix<T>>): Parent {
            val container = VBox()
            container.styleClass.add("fix-list")
            for (f in fixes) {
                container.children.add(fixLabel(f))
            }
            return container
        }

        private fun fixLabel(fix: ProblemFix<T>): Node = Button().apply {
            styleClass.add("problem-fix")
            text = fix.description
            isFocusTraversable = true
            registerShortcut(KeyCodeCombination(ENTER)) { applyAndHide(fix) }
            setOnMouseClicked { applyAndHide(fix) }
        }

        private fun applyAndHide(fix: ProblemFix<T>) {
            with(fix) {
                context.executeSafely("Executing ${fix.description}", Unit) {
                    target.fix()
                }
            }
            hide()
            ownerWindow.hide()
        }
    }
}