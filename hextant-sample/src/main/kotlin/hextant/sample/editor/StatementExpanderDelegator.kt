package hextant.sample.editor

import hextant.core.editor.ExpanderConfigurator

object StatementExpanderDelegator : ExpanderConfigurator<StatementEditor<*>>({
    "print" expand ::PrintStatementEditor
    "call" expand { ctx ->
        ExprStatementEditor(ctx).apply {
            expr.setEditor(FunctionCallEditor(ctx), undoable = false)
        }
    }
    "def" expand ::DefinitionEditor
    "assign" expand ::AssignmentEditor
    "aug" expand ::AugmentedAssignmentEditor
    "block" expand ::BlockEditor
    "break" expand { ctx -> ControlFlowStatementEditor(ctx, "break") }
    "continue" expand { ctx -> ControlFlowStatementEditor(ctx, "continue") }
    "return" expand ::ReturnStatementEditor
    "if" expand ::IfStatementEditor
    "while" expand ::WhileLoopEditor
    "for" expand ::ForLoopEditor
})