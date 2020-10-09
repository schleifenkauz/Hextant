package hextant.sample.editor

import hextant.context.withoutUndo
import hextant.core.editor.ExpanderConfigurator

object StatementExpanderDelegator : ExpanderConfigurator<StatementEditor<*>>({
    "print" expand ::PrintStatementEditor
    "call" expand { ctx ->
        ExprStatementEditor(ctx).withoutUndo {
            expr.setEditor(FunctionCallEditor(ctx))
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