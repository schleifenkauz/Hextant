package hextant.simple

import hextant.core.editor.ExpanderConfigurator
import hextant.simple.editor.*

object StatementExpanderDelegator : ExpanderConfigurator<StatementEditor<*>>({
    "print" += ::PrintStatementEditor
    "call" += { ctx ->
        ExprStatementEditor(ctx).apply {
            expr.setEditor(FunctionCallEditor(ctx), undoable = false)
        }
    }
    "def" += ::DefinitionEditor
    "assign" += ::AssignmentEditor
    "aug" += ::AugmentedAssignmentEditor
    "block" += ::BlockEditor
    "break" += { ctx -> ControlFlowStatementEditor(ctx, "break") }
    "continue" += { ctx -> ControlFlowStatementEditor(ctx, "continue") }
    "return" += ::ReturnStatementEditor
    "if" += ::IfStatementEditor
    "while" += ::WhileLoopEditor
    "for" += ::ForLoopEditor
})