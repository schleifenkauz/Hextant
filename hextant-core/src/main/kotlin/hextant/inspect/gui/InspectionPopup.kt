/**
 *@author Nikolaus Knop
 */

package hextant.inspect.gui

import hextant.Context
import hextant.fx.registerShortcut
import hextant.get
import hextant.impl.Stylesheets
import hextant.inspect.Problem
import hextant.inspect.ProblemFix
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.input.KeyCode.ENTER
import javafx.scene.input.KeyCodeCombination
import javafx.scene.layout.VBox
import javafx.stage.Popup

class InspectionPopup(private val context: Context, problems: () -> Set<Problem>) : Popup() {
    private val problems = { problems().sortedBy { it.severity } }

    private val container = VBox()

    init {
        context[Stylesheets].apply(scene)
        container.styleClass.add("problem-list")
        setOnShowing { update() }
        setOnHidden { ownerNode.requestFocus() }
        scene.root = container
    }

    private fun update() {
        container.children.clear()
        for (p in problems()) {
            container.children.add(problemLabel(p))
        }
    }

    private fun problemLabel(problem: Problem): Node = Button().apply {
        styleClass.add("problem")
        styleClass.add("${problem.severity}-item")
        text = problem.message
        isFocusTraversable = true
        val fixes = problem.fixes
        if (fixes.isNotEmpty()) {
            val fixesPopup = FixesPopup(context, fixes)
            setOnAction { showFixes(fixesPopup) }
            registerShortcut(KeyCodeCombination(ENTER)) { showFixes(fixesPopup) }
        }
    }

    private fun Button.showFixes(fixesPopup: FixesPopup) {
        val owner = scene.root
        val anchor = owner.localToScreen(boundsInLocal)
        fixesPopup.show(owner, anchor.maxX, anchor.minY)
    }

    override fun show() {
        if (problems().isNotEmpty()) {
            super.show()
        }
        if (isShowing) {
            val firstProblem = container.children.first()
            firstProblem.requestFocus()
        }
    }

    private class FixesPopup(context: Context, private val fixes: Collection<ProblemFix>) : Popup() {
        init {
            context[Stylesheets].apply(scene)
            content.add(createFixList(fixes))
        }

        override fun show() {
            if (fixes.isNotEmpty()) super.show()
        }

        private fun createFixList(fixes: Collection<ProblemFix>): Parent {
            val container = VBox()
            container.styleClass.add("fix-list")
            for (f in fixes) {
                container.children.add(fixLabel(f))
            }
            return container
        }

        private fun fixLabel(fix: ProblemFix): Node = Button().apply {
            styleClass.add("problem-fix")
            text = fix.description
            isDisable = !fix.isApplicable()
            isFocusTraversable = true
            registerShortcut(KeyCodeCombination(ENTER)) { applyAndHide(fix) }
            setOnMouseClicked { applyAndHide(fix) }
        }

        private fun applyAndHide(fix: ProblemFix) {
            fix.fix()
            hide()
            ownerWindow.hide()
        }
    }
}