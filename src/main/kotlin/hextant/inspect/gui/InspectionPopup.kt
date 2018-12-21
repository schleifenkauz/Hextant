/**
 *@author Nikolaus Knop
 */

package hextant.inspect.gui

import hextant.fx.registerShortcut
import hextant.fx.show
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

class InspectionPopup (private val owner: Node, problems: () -> Set<Problem>) : Popup() {
    private val problems = { problems().sortedBy { it.severity } }

    private val container = VBox()

    init {
        Stylesheets.apply(scene)
        container.styleClass.add("problem-list")
        setOnShowing { update() }
        content.add(container)
    }

    private fun update() {
        container.children.clear()
        for (p in problems()) {
            container.children.add(problemLabel(p))
        }
    }

    private fun problemLabel(problem: Problem): Node = Button().apply {
        styleClass.add("problem")
        styleClass.add(problem.severity.toString())
        text = problem.message
        isFocusTraversable = true
        val fixes = problem.fixes
        if (fixes.isNotEmpty()) {
            val fixesPopup = FixesPopup(fixes)
            registerShortcut(KeyCodeCombination(ENTER)) { fixesPopup.show(owner) }
            setOnAction { fixesPopup.show(owner) }
        }
    }

    private class FixesPopup(fixes: Collection<ProblemFix>) : Popup() {
        init {
            Stylesheets.apply(scene)
            content.add(createFixList(fixes))
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
            registerShortcut(KeyCodeCombination(ENTER)) { fix.fix() }
            setOnMouseClicked { fix.fix() }
        }
    }
}